package com.example.wmz.myapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements WordItemFragment.OnFragmentInteractionListener, WordDetailFragment.OnFragmentInteractionListener {

    private String reply="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //新增单词
                InsertDialog();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        WordsDB wordsDB=WordsDB.getWordsDB();
        if (wordsDB != null)
            wordsDB.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_onlineSearch:
                //在线查询单词含义
                onlineDialog();
                return true;

            case R.id.action_Search:
                //查找单词
                SearchDialog();
                return true;

            case R.id.action_Insert:
                //新增单词
                InsertDialog();
                return true;

            case R.id.action_Help:
                //帮助
                Toast toast=Toast.makeText(MainActivity.this,"欢迎使用XX单词本，这是帮助。", Toast.LENGTH_SHORT);
                showMyToast(toast,3*1000);
                return true;

            case R.id.action_Exit:
                //退出
                System.exit(0);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    //新增单词的对话框
    private void InsertDialog() {
        final TableLayout tableLayout = (TableLayout) getLayoutInflater().inflate(R.layout.insert, null);
        new AlertDialog.Builder(this)
                .setTitle("新增单词")//标题
                .setView(tableLayout)//设置视图
                //确定按钮及其动作
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String strWord = ((EditText) tableLayout.findViewById(R.id.txtWord)).getText().toString();
                        String strMeaning = ((EditText) tableLayout.findViewById(R.id.txtMeaning)).getText().toString();
                        String strSample = ((EditText) tableLayout.findViewById(R.id.txtSample)).getText().toString();
                        WordsDB wordsDB=WordsDB.getWordsDB();
                        wordsDB.Insert(strWord, strMeaning, strSample);
                        //单词已经插入到数据库，更新显示列表
                        RefreshWordItemFragment();
                    }
                })
                //取消按钮及其动作
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .create()//创建对话框
                .show();//显示对话框
    }

    //删除单词的对话框
    private void DeleteDialog(final String strId) {
        new AlertDialog.Builder(this).setTitle("删除单词")
                .setMessage("是否删除单词?")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        WordsDB wordsDB=WordsDB.getWordsDB();
                        wordsDB.DeleteUseSql(strId);
                        //单词已经删除，更新显示列表
                        RefreshWordItemFragment();
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        })
                .create()//创建对话框
                .show();//显示对话框
    }

    //修改单词的对话框
    private void UpdateDialog(final String strId, final String strWord, final String strMeaning, final String strSample) {
        final TableLayout tableLayout = (TableLayout) getLayoutInflater().inflate(R.layout.insert, null);
        ((EditText) tableLayout.findViewById(R.id.txtWord)).setText(strWord);
        ((EditText) tableLayout.findViewById(R.id.txtMeaning)).setText(strMeaning);
        ((EditText) tableLayout.findViewById(R.id.txtSample)).setText(strSample);
        new AlertDialog.Builder(this)
                .setTitle("修改单词")//标题
                .setView(tableLayout)//设置视图
                //确定按钮及其动作
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String strNewWord = ((EditText) tableLayout.findViewById(R.id.txtWord)).getText().toString();
                        String strNewMeaning = ((EditText) tableLayout.findViewById(R.id.txtMeaning)).getText().toString();
                        String strNewSample = ((EditText) tableLayout.findViewById(R.id.txtSample)).getText().toString();
                        WordsDB wordsDB=WordsDB.getWordsDB();
                        wordsDB.UpdateUseSql(strId, strNewWord, strNewMeaning, strNewSample);
                        //单词已经更新，更新显示列表
                        RefreshWordItemFragment();
                    }
                })
                //取消按钮及其动作
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .create()//创建对话框
                .show();//显示对话框
    }

    //查找单词的对话框
    private void SearchDialog() {
        final TableLayout tableLayout = (TableLayout) getLayoutInflater().inflate(R.layout.searchterm, null);
        new AlertDialog.Builder(this)
                .setTitle("查找单词")//标题
                .setView(tableLayout)//设置视图
                //确定按钮及其动作
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String txtSearchWord = ((EditText) tableLayout.findViewById(R.id.txtSearchWord)).getText().toString();
                        //单词已经插入到数据库，更新显示列表
                        RefreshWordItemFragment(txtSearchWord);
                    }
                })
                //取消按钮及其动作
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .create()//创建对话框
                .show();//显示对话框
    }

    // 当用户在单词详细Fragment中单击时回调此函数
    @Override
    public void onWordDetailClick(Uri uri) {

    }

    //当用户在单词列表Fragment中单击某个单词时回调此函数
    // 判断如果横屏的话，则需要在右侧单词详细Fragment中显示
    @Override
    public void onWordItemClick(String id) {
        if(isLand()) {//判断是否是横屏，横屏的话则在右侧的WordDetailFragment中显示单词详细信息
            ChangeWordDetailFragment(id);
        }else{
            Intent intent = new Intent(MainActivity.this,WordDetailActivity.class);
            intent.putExtra(WordDetailFragment.ARG_ID, id);
            startActivity(intent);
        }
    }

    private void ChangeWordDetailFragment(String id){
        Bundle arguments = new Bundle();
        arguments.putString(WordDetailFragment.ARG_ID, id);
        WordDetailFragment fragment = new WordDetailFragment();
        fragment.setArguments(arguments);
        getFragmentManager().beginTransaction().replace(R.id.worddetail, fragment).addToBackStack(null).commitAllowingStateLoss();
    }

    @Override
    public void onDeleteDialog(String strId) {
        DeleteDialog(strId);
    }

    @Override
    public void onUpdateDialog(String strId) {
        WordsDB wordsDB=WordsDB.getWordsDB();
        if (wordsDB != null && strId != null) {
            Words.WordDescription item = wordsDB.getSingleWord(strId);
            if (item != null) {
                UpdateDialog(strId, item.word, item.meaning, item.sample);
            }
        }
    }

    //在线查询查询单词
    private void onlineDialog(){
        final TableLayout tableLayout = (TableLayout) getLayoutInflater().inflate(R.layout.online_search, null);
        new AlertDialog.Builder(this)
                .setTitle("在线查询")//标题
                .setView(tableLayout)//设置视图
                //确定按钮及其动作
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String onlineSearchWord = ((EditText) tableLayout.findViewById(R.id.online_search_word)).getText().toString();
                        //有道查询
                        SearchWordsUseYouDao(onlineSearchWord);
                    }
                })
                //取消按钮及其动作
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .create()//创建对话框
                .show();//显示对话框
    }

    //使用有道查询单词含义
    private void SearchWordsUseYouDao(final String word){
        final Handler errorHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String notice = "";
                switch (msg.what) {
                    case 20:
                        notice = "要翻译的文本过长";
                        break;

                    case 30:
                        notice = "无法进行有效的翻译";
                        break;

                    case 40:
                        notice = "不支持的语言类型";
                        break;

                    case 50:
                        notice = "无效的key";
                        break;

                    case 60:
                        notice = "无词典结果,仅在获取词典结果生效";
                        break;

                    default:
                        break;

                }
                Toast toast=Toast.makeText(MainActivity.this,notice, Toast.LENGTH_SHORT);
                showMyToast(toast,3*1000);
            }
        };
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {

                if (msg.what == 1) {
                    Toast toast=Toast.makeText(MainActivity.this,reply, Toast.LENGTH_LONG);
                    showMyToast(toast,6*1000);
                } else if (msg.what == 0) {
                    Toast.makeText(MainActivity.this, "查询单词失败，请检查是否联网。", Toast.LENGTH_SHORT).show();
                }
            }
        };

        //进行json格式解析
        Runnable askForYouDao = new Runnable() {
            @Override
            public void run() {
                try {
                    String urlPath = "http://fanyi.youdao.com/openapi.do?keyfrom=haobaoshui&key=1650542691&type=data&doctype=json&version=1.1&q="
                            + URLEncoder.encode(word, "utf-8");
                    URL getUrl = new URL(urlPath);
                    HttpURLConnection connection = (HttpURLConnection) getUrl.openConnection();
                    connection.setConnectTimeout(3000);
                    connection.connect();
                    BufferedReader replyReader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));//约定输入流的编码
                    reply = replyReader.readLine();
                    JSONObject replyJson = new JSONObject(reply);
                    String errorCode = replyJson.getString("errorCode");
                    if (errorCode.equals("0")) {
                        String query = replyJson.getString("query");
                        JSONArray translation = replyJson.has("translation") ? replyJson.getJSONArray("translation") : null;
                        JSONObject basic = replyJson.has("basic") ? replyJson.getJSONObject("basic") : null;
                        String pronation=null;
                        String UK_pronation=null;
                        String US_pronation=null;
                        JSONArray explains=null;
                        if(basic!=null){
                            pronation=basic.has("pronation")? basic.getString("pronation"):null;
                            UK_pronation=basic.has("UK-pronation")? basic.getString("UK-pronation"):null;
                            US_pronation=basic.has("US-pronation")? basic.getString("US-pronation"):null;
                            explains=basic.has("explains")? basic.getJSONArray("explains"):null;
                        }
                        String translationString="";
                        if(translation!=null){
                            translationString="\n翻译：\n";
                            for(int i=0;i<translation.length();i++){
                                translationString+="\t"+translation.getString(i)+"\n";
                            }
                        }
                        String pronationString=(pronation!=null? "\n发音："+pronation:"")
                                +(UK_pronation!=null? "\n英式发音："+UK_pronation:"")
                                +(US_pronation!=null? "\n美式发音："+US_pronation:"");
                        String explainString="";
                        if(explains!=null){
                            explainString="\n其他释义：\n";
                            for(int i=0;i<explains.length();i++){
                                explainString+="\t"+explains.getString(i)+"\n";
                            }
                        }
                        reply="单词："+query+"\n"+translationString+pronationString+explainString;
                        WordsDB wordsDB=WordsDB.getWordsDB();
                        wordsDB.Insert(query,explainString,null);
                        handler.sendEmptyMessage(1);
                    } else {
                        int what = Integer.parseInt(errorCode);
                        errorHandler.sendEmptyMessage(what);
                    }
                } catch (Exception e) {
                    handler.sendEmptyMessage(0);
                }
            }
        };
        Thread thread=new Thread(askForYouDao);
        thread.start();
    }

    //设置toast显示时间
    public void showMyToast(final Toast toast, final int cnt) {
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                toast.show();
            }
        }, 0, 3500);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                toast.cancel();
                timer.cancel();
            }
        }, cnt );
    }

    //更新单词列表
    private void RefreshWordItemFragment() {
        WordItemFragment wordItemFragment = (WordItemFragment) getFragmentManager().findFragmentById(R.id.wordslist);
        wordItemFragment.refreshWordsList();
    }

    //更新单词列表
    private void RefreshWordItemFragment(String strWord) {
        WordItemFragment wordItemFragment = (WordItemFragment) getFragmentManager().findFragmentById(R.id.wordslist);
        wordItemFragment.refreshWordsList(strWord);
    }

    //判断是否是横屏
    private boolean isLand(){
        if(getResources().getConfiguration().orientation== Configuration.ORIENTATION_LANDSCAPE)
            return true;
        return false;
    }
}
