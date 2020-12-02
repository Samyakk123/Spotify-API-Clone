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
	  ObjectMapper mapper = new ObjectMapper();
	  HttpUrl.Builder urlBuilder = HttpUrl.parse("http://localhost:3001" + "/getSongTitleById/" + songId).newBuilder();
	  
//	  urlBuilder.addQueryParameter("songId", songId);
	  
	  // urlBuilder.addPathSegment(songId); ^^?
	  
	  
	  String url = urlBuilder.build().toString();
	  
//	  RequestBody body = RequestBody.create(null, new byte[0]);

	  Request request = new Request.Builder().url(url).build();
	  Call call = client.newCall(request);
	  
	  Response responseFromAddMs = null;
	  String addServiceBody = "{}";
	  
	  try {

	    responseFromAddMs = call.execute();

	    
	    
	    addServiceBody = responseFromAddMs.body().string();
	    
	    
	    mapper.readValue(addServiceBody, Map.class);
	    
	    
	    String song = (String) mapper.readValue(addServiceBody, Map.class).get("data");

	    // Verify that it was returned
	    if(song == null) {
	      toReturn.setdbQueryExecResult(DbQueryExecResult.QUERY_ERROR_GENERIC);
	      return toReturn;
	    }
	    
	      
	      try (Session session = ProfileMicroserviceApplication.driver.session()) {
	        try (Transaction trans = session.beginTransaction()) {
	          System.out.println("hi"); 
	             Map<String, Object> toInsert = new HashMap<String, Object>(); 
	             toInsert.put("userName", userName);
	             toInsert.put("plName", userName + "-favorites");
	             toInsert.put("song", song);
	             
	             trans.run("\n MATCH (a:profile {userName:$userName})-[r:created]->(b:playlist {plName:$plName})\n"
	                 + " MERGE (b)-[d:favorites]-(c:song {title:$song})", toInsert);
	             trans.success();
	             
//	             MATCH (a:profile {userName: "test"})-[r:created]->(b:playlist {plName:"test-favorites"})
//	             MERGE (b)-[d:created]-(c:song {title: "I should have came"})
	             
	        }
	        session.close();
	        return toReturn; 
	        
	      }    

	    
	    
	    
//	    toReturn.setdbQueryExecResult(DbQueryExecResult.QUERY_OK);
//	    toReturn.setData("OK");
	    
	    
	  }catch(Exception e) {
	    System.out.println("error: " + e);
	    toReturn.setdbQueryExecResult(DbQueryExecResult.QUERY_ERROR_GENERIC);
	  }
	  
	  return toReturn;
	}
	

	

	@Override
	public DbQueryStatus unlikeSong(String userName, String songId) {
		
		return null;
	}

	@Override
	public DbQueryStatus deleteSongFromDb(String songId) {
		
		return null;
	}
}
