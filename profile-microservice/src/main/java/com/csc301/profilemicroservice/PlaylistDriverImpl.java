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
	  
	  // Set initially to OK [Just to pre-define it]
	  toReturn = new DbQueryStatus("", DbQueryExecResult.QUERY_OK);
	  String song = "";

	  try (Session session = ProfileMicroserviceApplication.driver.session()) {
	    try (Transaction trans = session.beginTransaction()) {
	      
	      // Checks if it is already pre-existing in the neo4j database
	      Map<String, Object> checkValid = new HashMap<String, Object>();
	      Map<String, Object> checkValidTwo = new HashMap<String, Object>();
	      checkValid.put("songId", songId);
	      checkValid.put("plName", userName + "-favorites");
	      checkValidTwo.put("plName", userName + "-favorites"); 
	      Iterator<Record> checkUserName = trans.run("MATCH (a:playlist {plName:$plName})\n RETURN a", checkValidTwo);
	      if(!checkUserName.hasNext()) { 
	        toReturn.setdbQueryExecResult(DbQueryExecResult.QUERY_ERROR_GENERIC);
	        return toReturn; 
	      }
	      Iterator<Record> variable = trans.run("MATCH (a:playlist {plName:$plName})-[r:includes]->(b:song {id:$songId})\n RETURN r", checkValid);
	      // Check if a result was found
	      if(variable.hasNext()) {
	        return toReturn;
	      }
	      
	      
	      // Creating the microservice route similar to done in tutorial
	      ObjectMapper mapper = new ObjectMapper();
	      // This one is to get the song title name
	      HttpUrl.Builder urlBuilder = HttpUrl.parse("http://localhost:3001" + "/getSongTitleById/" + songId).newBuilder();
	      
	      ObjectMapper mapper2 = new ObjectMapper();
	      // Hardcoding the route
	      // This one is to update the favourites count
	      HttpUrl.Builder urlBuilder2 = HttpUrl.parse("http://localhost:3001" +  "/updateSongFavouritesCount/" + songId + "/?shouldDecrement=false").newBuilder();
	      
	      String url = urlBuilder.build().toString();
	      String url2 = urlBuilder2.build().toString();
	            
	      RequestBody body = RequestBody.create(null, new byte[0]);

	      Request request = new Request.Builder().url(url).build();
	      Request request2 = new Request.Builder().url(url2).method("PUT", body).build();
	      
	      // Call the client with the request calls of the two
	      Call call = client.newCall(request);
	      Call call2 = client.newCall(request2);
	      
	      Response responseFromAddMs = null;
	      String addServiceBody = "{}";
	      
	      Response responseFromAddMs2 = null;
	      String addServiceBody2 = "{}";
	      
	      try {

	        // Call the Song microservices
	        responseFromAddMs = call.execute();
	        responseFromAddMs2 = call2.execute();
	  
	        addServiceBody = responseFromAddMs.body().string();
	        addServiceBody2 = responseFromAddMs2.body().string();
	        
	        // Save the song title in a variable 
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
	      
	      
	      // Create the NEO4J command to create a relationship between song and user playlist
	      
	      Map<String, Object> toInsert = new HashMap<String, Object>(); 
	      toInsert.put("userName", userName);
	      toInsert.put("plName", userName + "-favorites");
	      toInsert.put("song", song);
	      toInsert.put("id", songId);
	             
	             
	      trans.run("\n MATCH (a:profile {userName:$userName})-[r:created]->(b:playlist {plName:$plName})\n"
	                 + " MERGE (b)-[d:includes]-(c:song {title:$song, id: $id})", toInsert);
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
		    
		    // Check null case [Whether the relation actually does exist]
		    Iterator<Record> variable = trans.run("MATCH (a:playlist {plName:$plName})-[r:includes]->(b:song {id:$songId})\n RETURN r", toRemove);
            if(!variable.hasNext()) {
              toReturn.setdbQueryExecResult(DbQueryExecResult.QUERY_ERROR_GENERIC);
              return toReturn;
            }
		    // Delete the relationship between the playlist and the song node
		    trans.run("MATCH (a:playlist {plName:$plName})-[r:includes]->(b:song {id:$songId})\n"
		        + "DELETE r", toRemove);
		    // Then delete the song node afterwards
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
	        // Make the client Call
	        Call call = client.newCall(request);
	        
	        Response responseFromAddMs = null;
	        String addServiceBody = "{}";
	        
	        try {
	          // execute the call
	          responseFromAddMs = call.execute();
	          addServiceBody = responseFromAddMs.body().string();
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
	      // Create a map with songId
	      Map<String, Object> deleteAll = new HashMap<String, Object>();
	      deleteAll.put("songId", songId);
	      // DETACH all relationships between the song node and then delete it
	      trans.run("MATCH (a:song {id:$songId})\n" + "DETACH DELETE a", deleteAll);
	      trans.success();
	    }catch(Exception e) {
	      toReturn.setdbQueryExecResult(DbQueryExecResult.QUERY_ERROR_GENERIC);
	    }
	    // close the session if it worked
	    session.close();
	  }catch(Exception e) {
	    toReturn.setdbQueryExecResult(DbQueryExecResult.QUERY_ERROR_GENERIC);
	  }
	  return toReturn;
	}
}
