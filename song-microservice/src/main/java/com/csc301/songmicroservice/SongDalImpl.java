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
import com.mongodb.BasicDBObject;

@Repository
public class SongDalImpl implements SongDal {

	private final MongoTemplate db;
	private DbQueryStatus toReturn = null;

	@Autowired
	public SongDalImpl(MongoTemplate mongoTemplate) {
		this.db = mongoTemplate;
	}

	@Override
	public DbQueryStatus addSong(Song songToAdd) {
		// TODO Auto-generated method stub
//	    Map<String, String> temp = songToAdd.getJsonRepresentation();
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DbQueryStatus getSongTitleById(String songId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DbQueryStatus deleteSongById(String songId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DbQueryStatus updateSongFavouritesCount(String songId, boolean shouldDecrement) {
		// TODO Auto-generated method stub
		return null;
	}
}