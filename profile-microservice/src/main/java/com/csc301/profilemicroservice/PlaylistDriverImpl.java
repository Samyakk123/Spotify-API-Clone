package com.csc301.profilemicroservice;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.springframework.stereotype.Repository;


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
	  HttpUrl.Builder urlBuilder = HttpUrl.parse("http://localhost:3001" + "/getSongTitleById/" + songId).newBuilder();
	  
//	  urlBuilder.addQueryParameter("songId", songId);
	  
	  // urlBuilder.addPathSegment(songId); ^^?
	  
	  
	  String url = urlBuilder.build().toString();
	  System.out.println("important line here: " +  url);
	  
//	  RequestBody body = RequestBody.create(null, new byte[0]);

	  Request request = new Request.Builder().url(url).build();
	  Call call = client.newCall(request);
	  
	  Response responseFromAddMs = null;
	  String addServiceBody = "{}";
	  
	  try {

	    responseFromAddMs = call.execute();

	    addServiceBody = responseFromAddMs.body().string();
	    System.out.println(addServiceBody);

	    toReturn.setdbQueryExecResult(DbQueryExecResult.QUERY_OK);
	    toReturn.setData("OK");
	    
	    
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
