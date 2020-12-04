package com.csc301.profilemicroservice;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.springframework.stereotype.Repository;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


import org.neo4j.driver.v1.Transaction;

@Repository
public class PlaylistDriverImpl implements PlaylistDriver {

	Driver driver = ProfileMicroserviceApplication.driver;
	
	private DbQueryStatus toReturn = null; 
	OkHttpClient client = new OkHttpClient();

	public static void InitPlaylistDb() {
		String queryStr;

		try (Session session = ProfileMicroserviceApplication.driver.session()) {
			try (Transaction trans = session.beginTransaction()) {
				queryStr = "CREATE CONSTRAINT ON (nPlaylist:playlist) ASSERT exists(nPlaylist.plName)";
				trans.run(queryStr);
				trans.success();
			}
			session.close();
		}
	}

	@Override
	public DbQueryStatus likeSong(String userName, String songId) {
	  
	  
	  toReturn = new DbQueryStatus("", DbQueryExecResult.QUERY_OK);
	  String song = "";

	    
	      
	  try (Session session = ProfileMicroserviceApplication.driver.session()) {
	    try (Transaction trans = session.beginTransaction()) {
	      
	      // Checks if it is already pre-existing
	      Map<String, Object> checkValid = new HashMap<String, Object>();
	      checkValid.put("songId", songId);
	      checkValid.put("plName", userName + "-favorites");
	      
	      
	      Iterator<Record> variable = trans.run("MATCH (a:playlist {plName:$plName})-[r:favorites]->(b:song {id:$songId})\n RETURN r", checkValid);
	      if(variable.hasNext()) {
	        return toReturn;
	      }
	      
	      
	      
	      ObjectMapper mapper = new ObjectMapper();
	      HttpUrl.Builder urlBuilder = HttpUrl.parse("http://localhost:3001" + "/getSongTitleById/" + songId).newBuilder();
	      
	      ObjectMapper mapper2 = new ObjectMapper();
	      HttpUrl.Builder urlBuilder2 = HttpUrl.parse("http://localhost:3001" +  "/updateSongFavouritesCount/" + songId + "/?shouldDecrement=false").newBuilder();
	      
//	    urlBuilder.addQueryParameter("songId", songId);     
	      // urlBuilder.addPathSegment(songId); ^^?
	      
	      
	      String url = urlBuilder.build().toString();
	      String url2 = urlBuilder2.build().toString();
	      
	      
	      RequestBody body = RequestBody.create(null, new byte[0]);

	      Request request = new Request.Builder().url(url).build();
	      Request request2 = new Request.Builder().url(url2).method("PUT", body).build();
	      
	      Call call = client.newCall(request);
	      Call call2 = client.newCall(request2);
	      
	      Response responseFromAddMs = null;
	      String addServiceBody = "{}";
	      
	      Response responseFromAddMs2 = null;
	      String addServiceBody2 = "{}";
	      
	      try {


	        responseFromAddMs = call.execute();
	        responseFromAddMs2 = call2.execute();
	  
	        addServiceBody = responseFromAddMs.body().string();
	        addServiceBody2 = responseFromAddMs2.body().string();
	        
	  
	        
	        
	  
	        song = (String) mapper.readValue(addServiceBody, Map.class).get("data");
	        // Verify that it was returned
	        if(song == null) {
	          toReturn.setdbQueryExecResult(DbQueryExecResult.QUERY_ERROR_GENERIC);
	          return toReturn;
	        }
	      }catch(Exception e) {
	        toReturn.setdbQueryExecResult(DbQueryExecResult.QUERY_ERROR_GENERIC);
	        return toReturn;
	      }
	      
	      
	      
	      
	      Map<String, Object> toInsert = new HashMap<String, Object>(); 
	      toInsert.put("userName", userName);
	      toInsert.put("plName", userName + "-favorites");
	      toInsert.put("song", song);
	      toInsert.put("id", songId);
	             
	             
	      trans.run("\n MATCH (a:profile {userName:$userName})-[r:created]->(b:playlist {plName:$plName})\n"
	                 + " MERGE (b)-[d:favorites]-(c:song {title:$song, id: $id})", toInsert);
	      trans.success();
	             
	    }catch(Exception e) {
	      toReturn.setdbQueryExecResult(DbQueryExecResult.QUERY_ERROR_GENERIC);
	    }
	      session.close();
	        
	  }catch(Exception e) {
	    toReturn.setdbQueryExecResult(DbQueryExecResult.QUERY_ERROR_GENERIC);

	  }    
	      
	  return toReturn;

	}
	

	

	@Override
	public DbQueryStatus unlikeSong(String userName, String songId) {
		toReturn = new DbQueryStatus("", DbQueryExecResult.QUERY_OK);
		


		
		try(Session session = ProfileMicroserviceApplication.driver.session()){
		  try(Transaction trans = session.beginTransaction()){
		    Map<String, Object> toRemove = new HashMap<String, Object>();
		    Map<String, Object> toRemove2 = new HashMap<String, Object>();
		    toRemove.put("plName", userName + "-favorites");
		    toRemove.put("songId", songId);
		    toRemove2.put("id", songId);
		    
		    // Check null case
		    Iterator<Record> variable = trans.run("MATCH (a:playlist {plName:$plName})-[r:favorites]->(b:song {id:$songId})\n RETURN r", toRemove);
            if(!variable.hasNext()) {
              toReturn.setdbQueryExecResult(DbQueryExecResult.QUERY_ERROR_GENERIC);
              return toReturn;
            }
		    
		    trans.run("MATCH (a:playlist {plName:$plName})-[r:favorites]->(b:song {id:$songId})\n"
		        + "DELETE r", toRemove);
		    trans.run("MATCH (a:song {id:$id})\n"
		        + "DELETE a", toRemove2);
		    
		    trans.success();

		    
		    // Subtract by one in favourites of the song
	        ObjectMapper mapper = new ObjectMapper();
	        HttpUrl.Builder urlBuilder = HttpUrl.parse("http://localhost:3001" + "/updateSongFavouritesCount/" + songId + "/?shouldDecrement=true").newBuilder();
	        
	        urlBuilder.addQueryParameter("songId", songId);
	        
	        String url = urlBuilder.build().toString();
	        RequestBody body = RequestBody.create(null, new byte[0]);
	        
	        Request request = new Request.Builder().url(url).method("PUT", body).build();
	        
	        Call call = client.newCall(request);
	        
	        Response responseFromAddMs = null;
	        String addServiceBody = "{}";
	        
	        try {
	          responseFromAddMs = call.execute();
	          addServiceBody = responseFromAddMs.body().string();
	          System.out.println("here: " + addServiceBody);
	        }catch(Exception e) {
	          toReturn.setdbQueryExecResult(DbQueryExecResult.QUERY_ERROR_GENERIC);
	          return toReturn;
	        }
	        
		    

		  }
		  session.close();
		  
		}catch(Exception e) {
		  toReturn.setdbQueryExecResult(DbQueryExecResult.QUERY_ERROR_GENERIC);
		  
		}
		
		
		return toReturn;
		

	}

	@Override
	public DbQueryStatus deleteSongFromDb(String songId) {
	  toReturn = new DbQueryStatus("", DbQueryExecResult.QUERY_OK);
	  try(Session session = ProfileMicroserviceApplication.driver.session()){
	    try(Transaction trans = session.beginTransaction()){
	      Map<String, Object> deleteAll = new HashMap<String, Object>();
	      deleteAll.put("songId", songId);
	      
	      trans.run("MATCH (a:song {id:$songId})\n" + "DETACH DELETE a", deleteAll);
	      trans.success();
	    }catch(Exception e) {
	      toReturn.setdbQueryExecResult(DbQueryExecResult.QUERY_ERROR_GENERIC);
	    }
	    session.close();
	  }catch(Exception e) {
	    toReturn.setdbQueryExecResult(DbQueryExecResult.QUERY_ERROR_GENERIC);
	  }
	  
	  return toReturn;
	  
//	  MATCH (a:song {id: "5fc312a84079b40ab0e0264f"})
//	  DETACH DELETE a
	  
	}
}
