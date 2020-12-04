package com.csc301.songmicroservice;

import java.util.Map;
import org.bson.BasicBSONObject;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Repository
public class SongDalImpl implements SongDal {

    private final MongoTemplate db;
    private DbQueryStatus toReturn = null;
    
    OkHttpClient client = new OkHttpClient();

    @Autowired
    public SongDalImpl(MongoTemplate mongoTemplate) {
        this.db = mongoTemplate;
    }

    @Override
    public DbQueryStatus addSong(Song songToAdd) {
        // TODO Auto-generated method stub
//      Map<String, String> temp = songToAdd.getJsonRepresentation();
      toReturn = new DbQueryStatus("", DbQueryExecResult.QUERY_OK);
      
      try {
        db.insert(songToAdd, "songs");
        toReturn.setData(songToAdd);
        return toReturn;
      }catch(Exception e) {
        toReturn.setdbQueryExecResult(DbQueryExecResult.QUERY_ERROR_GENERIC);
        return toReturn;
      }
      
    }

    @Override
    public DbQueryStatus findSongById(String songId) {
      toReturn = new DbQueryStatus("", DbQueryExecResult.QUERY_OK);
      try {
        Song found = db.findById(songId, Song.class);
        if(found == null) { 
          toReturn.setdbQueryExecResult(DbQueryExecResult.QUERY_ERROR_NOT_FOUND); 
        }
        toReturn.setData(found);
        return toReturn;
      }catch(Exception e) {
        toReturn.setdbQueryExecResult(DbQueryExecResult.QUERY_ERROR_GENERIC);
        return toReturn;
      }
    }

    @Override
    public DbQueryStatus getSongTitleById(String songId) {
        // TODO Auto-generated method stub
      toReturn = new DbQueryStatus("", DbQueryExecResult.QUERY_OK);
      try {
        Song found = db.findById(songId, Song.class);
        if(found == null) { 
          toReturn.setdbQueryExecResult(DbQueryExecResult.QUERY_ERROR_NOT_FOUND); 
        }
        toReturn.setData(found);
        return toReturn;
      }catch(Exception e) {
        toReturn.setdbQueryExecResult(DbQueryExecResult.QUERY_ERROR_GENERIC);
        return toReturn;
      }
    }

    @Override
    public DbQueryStatus deleteSongById(String songId) {
        // TODO Auto-generated method stub
      toReturn = new DbQueryStatus("", DbQueryExecResult.QUERY_OK);

      
      ObjectMapper mapper = new ObjectMapper();
      HttpUrl.Builder urlBuilder = HttpUrl.parse("http://localhost:3002" + "/deleteAllSongsFromDb/" + songId).newBuilder();
      
      String url = urlBuilder.build().toString();
      RequestBody body = RequestBody.create(null, new byte[0]);
      Request request = new Request.Builder().url(url).method("PUT", body).build();
      
      Call call = client.newCall(request);
      Response responseFromAddMs = null;
      String addServiceBody = "{}";
      
      try {
        responseFromAddMs = call.execute();
        addServiceBody = responseFromAddMs.body().string();
      }catch(Exception e) {
        toReturn.setdbQueryExecResult(DbQueryExecResult.QUERY_ERROR_GENERIC);
        return toReturn;
      }
      
      try {
        ObjectId temp = new ObjectId(songId);
        db.getCollection("songs").deleteOne(Filters.eq("_id", temp));
        toReturn.setData("OK");
      }catch(Exception e) {
        toReturn.setdbQueryExecResult(DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
      }
      return toReturn;
    }

    @Override
    public DbQueryStatus updateSongFavouritesCount(String songId, boolean shouldDecrement) {

      toReturn = new DbQueryStatus("", DbQueryExecResult.QUERY_OK);
      
      DbQueryStatus status = this.findSongById(songId);
      Song returnVal = (Song) status.getData();

      
      long currentVal = returnVal.getSongAmountFavourites();
      long increment = 0;
      if(shouldDecrement) {
        increment = -1;
      }
      else {
        increment = 1;
      }
      returnVal.setSongAmountFavourites(currentVal + increment);

      try {
        
        // FIX INVALID INPUT CHECK HERE :( DON'T RMMR HOW DIVYAM DID IT and too sleepy
        ObjectId temp = new ObjectId(songId);
        db.getCollection("songs").updateOne(Filters.eq("_id", temp), Updates.set("songAmountFavourites", currentVal+increment));
        toReturn.setData(returnVal);
        toReturn.setdbQueryExecResult(DbQueryExecResult.QUERY_OK);
      }
      catch(Exception e) {
        toReturn.setdbQueryExecResult(DbQueryExecResult.QUERY_ERROR_GENERIC);
      }
      
      return toReturn;
      
      
//      DbQueryStatus status = songDal.findSongById(songId);
//      Song returnVal = (Song) status.getData();
//      
//      long currentVal = returnVal.getSongAmountFavourites();
//      
//      long increment = Integer.parseInt(shouldDecrement);
//
//      returnVal.setSongAmountFavourites(currentVal + increment);
      
    }
}