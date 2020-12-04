package com.csc301.profilemicroservice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

import org.springframework.stereotype.Repository;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.Value;

@Repository
public class ProfileDriverImpl implements ProfileDriver {

	Driver driver = ProfileMicroserviceApplication.driver;
	private DbQueryStatus toReturn = null; 

	public static void InitProfileDb() {
		String queryStr;

		try (Session session = ProfileMicroserviceApplication.driver.session()) {
			try (Transaction trans = session.beginTransaction()) {
				queryStr = "CREATE CONSTRAINT ON (nProfile:profile) ASSERT exists(nProfile.userName)";
				trans.run(queryStr);

				queryStr = "CREATE CONSTRAINT ON (nProfile:profile) ASSERT exists(nProfile.password)";
				trans.run(queryStr);

				queryStr = "CREATE CONSTRAINT ON (nProfile:profile) ASSERT nProfile.userName IS UNIQUE";
				trans.run(queryStr);

				trans.success();
			}
			session.close();
		}
	}
	
	@Override
	public DbQueryStatus createUserProfile(String userName, String fullName, String password) {
	  
	  toReturn = new DbQueryStatus("", DbQueryExecResult.QUERY_OK);
	  
	  try (Session session = ProfileMicroserviceApplication.driver.session()) {
	    try (Transaction trans = session.beginTransaction()) {
    	     Map<String, Object> toInsert = new HashMap<String, Object>(); 
    	     Map<String, Object>  toInsertTwo = new HashMap<String, Object>(); 
    	     Map<String, Object>  toInsertThree = new HashMap<String, Object>(); 
    	     toInsert.put("userName", userName); 
    	     toInsert.put("fullName", fullName); 
    	     toInsert.put("password", password); 
    	     toInsertTwo.put("plName", userName + "-favorites"); 
    	     toInsertThree.put("userName", userName); 
    	     toInsertThree.put("plName", userName + "-favorites"); 
    	     Iterator<Record> checkExisting = trans.run("MATCH (a:profile {userName: $userName, fullName: $fullName, password: $password})\nRETURN a", toInsert);
	         if(checkExisting.hasNext()) {
	           toReturn.setdbQueryExecResult(DbQueryExecResult.QUERY_ERROR_GENERIC);
	           return toReturn;
	         }
    	     trans.run("MERGE (a:profile {userName: $userName, fullName: $fullName, password: $password})", toInsert);

    	     trans.run("MERGE (a:playlist {plName:$plName})", toInsertTwo); 
    	     trans.run("MATCH (a:profile {userName: $userName}), (b:playlist {plName:$plName}) \n MERGE (a)-[r:created]->(b)", toInsertThree); 
    	     trans.success();
    	     
	    }
        session.close();
        return toReturn; 
        
      }
	  catch(Exception e){ 
	    toReturn.setdbQueryExecResult(DbQueryExecResult.QUERY_ERROR_GENERIC);
        return toReturn;
	  }
	}

	@Override
	public DbQueryStatus followFriend(String userName, String frndUserName) {
		
	  toReturn = new DbQueryStatus("", DbQueryExecResult.QUERY_OK);
	  try (Session session = ProfileMicroserviceApplication.driver.session()){
	    try( Transaction trans = session.beginTransaction()){
	      Map<String, Object> toInsert = new HashMap<String, Object>();
	      toInsert.put("userName", userName);
	      toInsert.put("frndUserName", frndUserName);
	      trans.run("MATCH (a:profile {userName:$userName}), (b:profile {userName:$frndUserName})\n" + "CREATE (a)-[r:FRIENDS]->(b)\n" + "RETURN type(r)", toInsert);
	      trans.success();
	    }
	    session.close();
	    return toReturn;
	  }catch(Exception e) {
	    toReturn.setdbQueryExecResult(DbQueryExecResult.QUERY_ERROR_GENERIC);
	    return toReturn;
	  }
	  
	  
		
	}

	@Override
	public DbQueryStatus unfollowFriend(String userName, String frndUserName) {
	  
      toReturn = new DbQueryStatus("", DbQueryExecResult.QUERY_OK);
      try (Session session = ProfileMicroserviceApplication.driver.session()){
        try( Transaction trans = session.beginTransaction()){
          Map<String, Object> toInsert = new HashMap<String, Object>();
          toInsert.put("userName", userName);
          toInsert.put("frndUserName", frndUserName);
          trans.run("MATCH (a:profile {userName:$userName})-[r:FRIENDS]->(b:profile {userName:$frndUserName})\n" + "DELETE r" , toInsert);
          trans.success();
          
//          MATCH (a:profile {userName: "awesome"})-[r:FRIENDS]->(b:profile {userName: "anotherName"})
//          DELETE r
          
          
        }
        session.close();
        return toReturn;
      }catch(Exception e) {
        toReturn.setdbQueryExecResult(DbQueryExecResult.QUERY_ERROR_GENERIC);
        return toReturn;
      }
	}

	@Override
	public DbQueryStatus getAllSongFriendsLike(String userName) {
	  toReturn = new DbQueryStatus("", DbQueryExecResult.QUERY_OK);
      try (Session session = ProfileMicroserviceApplication.driver.session()){
        try( Transaction trans = session.beginTransaction()){
          Map<String, Object> toInsert = new HashMap<String, Object>();
          Map<String, Object> data = new HashMap<String, Object>(); 
          
          toInsert.put("userName", userName);
          StatementResult responseOne = trans.run("MATCH (a:profile {userName:$userName})-[r:FRIENDS]->(b:profile) \n RETURN b.userName", toInsert);
          List<Record> records = responseOne.list();      
          for(int i = 0; i < records.size(); i++) {
            //assuming userName cannot be duplicate !!! 
            ArrayList<String> temp = new ArrayList<String>(); 
            Map<String, Object> toInsertTwo = new HashMap<String, Object>();
            Value friend = records.get(i).get("b.userName");
            String friendTemp = friend.toString().substring(1, friend.toString().length() - 1); 
            toInsertTwo.put("friend", friendTemp); 
            toInsertTwo.put("plName", friendTemp + "-favorites"); 
            StatementResult responseTwo = trans.run("MATCH (a:profile {userName:$friend})-[r:created]->(b:playlist {plName:$plName})"
                + "MATCH (b:playlist)-[d:favorites]->(c:song) \n "
                + "RETURN c.title", toInsertTwo);
            List<Record> recordsTwo = responseTwo.list(); 
            for(int j = 0; j < recordsTwo.size(); j++) { 
              Value songTitle = recordsTwo.get(j).get("c.title"); 
              temp.add(songTitle.toString().substring(1, songTitle.toString().length() - 1));
            }
            data.put(friendTemp, temp);
            
          }
          toReturn.setData(data);
          trans.success();
        }
        session.close();
        return toReturn;
      }catch(Exception e) {
        toReturn.setdbQueryExecResult(DbQueryExecResult.QUERY_ERROR_GENERIC);
        return toReturn;
      }
	}
}
