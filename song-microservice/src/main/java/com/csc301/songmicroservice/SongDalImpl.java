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
    toReturn = new DbQueryStatus("", DbQueryExecResult.QUERY_OK);
    // inserts the song to the database and gives a proper status message by modifying toReturn
    // accordingly and returning it
    try {
      db.insert(songToAdd, "songs");
      toReturn.setData(songToAdd);
      return toReturn;
    } catch (Exception e) {
      toReturn.setdbQueryExecResult(DbQueryExecResult.QUERY_ERROR_GENERIC);
      return toReturn;
    }

  }

  @Override
  public DbQueryStatus findSongById(String songId) {
    // Initially set it to OK
    toReturn = new DbQueryStatus("", DbQueryExecResult.QUERY_OK);
    try {
      // Find the database inside the database Song class using the provided songId
      Song found = db.findById(songId, Song.class);
      // Check does not exist case
      if (found == null) {
        toReturn.setdbQueryExecResult(DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
        return toReturn;
      }
      // return the found Song Object
      toReturn.setData(found);
      return toReturn;
    } catch (Exception e) {
      toReturn.setdbQueryExecResult(DbQueryExecResult.QUERY_ERROR_GENERIC);
      return toReturn;
    }
  }

  @Override
  public DbQueryStatus getSongTitleById(String songId) {
    toReturn = new DbQueryStatus("", DbQueryExecResult.QUERY_OK);
    try {
      Song found = db.findById(songId, Song.class);
      // Finds the song by id and if its not found then an status of not found is returned
      if (found == null) {
        toReturn.setdbQueryExecResult(DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
        return toReturn;
      }
      // If song is found then song is returned with the proper status "OK"
      toReturn.setData(found);
      return toReturn;
    } catch (Exception e) {
      toReturn.setdbQueryExecResult(DbQueryExecResult.QUERY_ERROR_GENERIC);
      return toReturn;
    }
  }

  @Override
  public DbQueryStatus deleteSongById(String songId) {
    // TODO Auto-generated method stub
    toReturn = new DbQueryStatus("", DbQueryExecResult.QUERY_OK);


    ObjectMapper mapper = new ObjectMapper();
    HttpUrl.Builder urlBuilder =
        HttpUrl.parse("http://localhost:3002" + "/deleteAllSongsFromDb/" + songId).newBuilder();

    String url = urlBuilder.build().toString();
    RequestBody body = RequestBody.create(null, new byte[0]);
    Request request = new Request.Builder().url(url).method("PUT", body).build();

    Call call = client.newCall(request);
    Response responseFromAddMs = null;
    String addServiceBody = "{}";

    try {
      responseFromAddMs = call.execute();
      addServiceBody = responseFromAddMs.body().string();
    } catch (Exception e) {
      toReturn.setdbQueryExecResult(DbQueryExecResult.QUERY_ERROR_GENERIC);
      return toReturn;
    }

    try {
      ObjectId temp = new ObjectId(songId);
      db.getCollection("songs").deleteOne(Filters.eq("_id", temp));
    } catch (Exception e) {
      toReturn.setdbQueryExecResult(DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
    }
    return toReturn;
  }

  @Override
  public DbQueryStatus updateSongFavouritesCount(String songId, boolean shouldDecrement) {

    toReturn = new DbQueryStatus("", DbQueryExecResult.QUERY_OK);
    //Finds song by id for which we need to increment / decrement 
    DbQueryStatus status = this.findSongById(songId);
    
    //If findSongById returns a generic error, then error is returned here before incrementing / decrementing 
    if(status.getdbQueryExecResult() != DbQueryExecResult.QUERY_OK) { 
      //Checks if Id is not a proper id format and changes the status to not found 
      try {
        ObjectId temp = new ObjectId(songId);
        toReturn.setdbQueryExecResult(status.getdbQueryExecResult());
      }
      catch(Exception e) { 
        toReturn.setdbQueryExecResult(DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
      }
      return toReturn; 
    } 
    Song returnVal = (Song) status.getData();
    
    //Increments or decrements depending on shouldDecrement value 
    long currentVal = returnVal.getSongAmountFavourites();
    long increment = 0;
    if (shouldDecrement) {
      increment = -1;
    } else {
      increment = 1;
    }
    returnVal.setSongAmountFavourites(currentVal + increment);
    //Assures that the songAmountFavourites value cannot go under 0
    if((currentVal + increment) < 0) { 
      returnVal.setSongAmountFavourites(0);
    }
    //Updates the database with the new increment 
    db.getCollection("songs").updateOne(Filters.eq("_id", new ObjectId(songId)),
        Updates.set("songAmountFavourites", currentVal + increment));
    toReturn.setData(returnVal);
    return toReturn;

  }
}
