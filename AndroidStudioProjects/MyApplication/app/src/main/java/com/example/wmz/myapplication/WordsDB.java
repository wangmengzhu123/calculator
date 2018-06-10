package com.example.wmz.myapplication;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class WordsDB {
    private static WordsDBHelper mDbHelper;
    private static WordsDB instance=new WordsDB();
    public static WordsDB getWordsDB(){
        return WordsDB.instance;
    }
    private WordsDB() {
        if (mDbHelper == null) {
            mDbHelper = new WordsDBHelper(WordsApplication.getContext());
        }
    }

    public void close() {
        if (mDbHelper != null)
            mDbHelper.close();
    }

    //获得单个单词的全部信息
    public Words.WordDescription getSingleWord(String id) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String sql = "select * from words where _ID=?";
        Cursor cursor = db.rawQuery(sql, new String[]{id});
        if (cursor.moveToNext()) {
            Words.WordDescription item = new Words.WordDescription(cursor.getString(cursor.getColumnIndex(Words.Word._ID)),
                    cursor.getString(cursor.getColumnIndex(Words.Word.COLUMN_NAME_WORD)),
                    cursor.getString(cursor.getColumnIndex(Words.Word.COLUMN_NAME_MEANING)),
                    cursor.getString(cursor.getColumnIndex(Words.Word.COLUMN_NAME_SAMPLE)));
            return item;
        }
        return null;
    }

    //得到全部单词列表
    public ArrayList<Map<String, String>> getAllWords() {
        if (mDbHelper == null) {
            return null;
        }
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String[] projection = {
                Words.Word._ID,
                Words.Word.COLUMN_NAME_WORD
        };
        //升序排序
        String sortOrder =
                Words.Word.COLUMN_NAME_WORD + " ASC";
        Cursor c = db.query(
                Words.Word.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                null,                                // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );
        return ConvertCursor2WordList(c);
    }

    //将游标转化为单词列表
    private ArrayList<Map<String, String>> ConvertCursor2WordList(Cursor cursor) {
        ArrayList<Map<String, String>> result = new ArrayList<>();
        while (cursor.moveToNext()) {
            Map<String, String> map = new HashMap<>();
            map.put(Words.Word._ID, String.valueOf(cursor.getString(cursor.getColumnIndex(Words.Word._ID))));
            map.put(Words.Word.COLUMN_NAME_WORD, cursor.getString(cursor.getColumnIndex(Words.Word.COLUMN_NAME_WORD)));
            result.add(map);
        }
        return result;
    }

    //使用insert方法增加单词
    public void Insert(String strWord, String strMeaning, String strSample) {
        //获取数据库的写权限
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        //创建一个新的值映射，其中列名是键
        ContentValues values = new ContentValues();
        values.put(Words.Word._ID, GUID.getGUID());
        values.put(Words.Word.COLUMN_NAME_WORD, strWord);
        values.put(Words.Word.COLUMN_NAME_MEANING, strMeaning);
        values.put(Words.Word.COLUMN_NAME_SAMPLE, strSample);
        // 插入新行，返回新行的主键值
        long newRowId;
        newRowId = db.insert(
                Words.Word.TABLE_NAME,
                null,
                values);
    }

    //使用Sql语句删除单词
    public void DeleteUseSql(String strId) {
        String sql = "delete from words where _id='" + strId + "'";
        //Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        db.execSQL(sql);
    }

    //使用Sql语句更新单词
    public void UpdateUseSql(String strId, String strWord, String strMeaning, String strSample) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String sql = "update words set word=?,meaning=?,sample=? where _id=?";
        db.execSQL(sql, new String[]{strWord,strMeaning, strSample, strId});
    }

    //使用Sql语句查找
    public ArrayList<Map<String, String>> SearchUseSql(String strWordSearch) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String sql = "select * from words where word like ? order by word ASC";
        Cursor c = db.rawQuery(sql, new String[]{"%" + strWordSearch + "%"});
        return ConvertCursor2WordList(c);
    }
}