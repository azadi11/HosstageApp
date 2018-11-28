package azadi.soheila.hostageapp;

import java.util.ArrayList;
import java.util.List;
import android.annotation.SuppressLint;
import android.app.Application;
import android.app.ListActivity;
import android.app.ProgressDialog;
import  android.content.Context ;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;



@SuppressLint({"InflateParams"})
public class MainActivity extends ListActivity {
    private PackageManager packageManager = null;
    private List<ApplicationInfo> applist = null;
    private ApplicationAdapter listadaptor=null;
    static String filename="app";
    String prefApps[];
    SharedPreferences sc;
    boolean[] appflag;
    int[]arr;
    int count=0;
    ImageView ivlock;
    Button bloc;
    ListView myList;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    @Override
    protected void onResume() {
        super.onResume();
        sc = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        packageManager = getPackageManager();

        new LoadApplications().execute();
        myList = getListView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        for (int i = 0; i < appflag.length; ++i) {
            if (appflag[i] == true) {
                count++;

            }
        }
        arr = new int[count];
        int a = 0;
        for (int i = 0; i < appflag.length; ++i) {
            if (appflag[i] == true) {
                arr[a] = i;
                a++;
            }
        }
        String prefapps = " ";
        for (int i = 0; i < count; ++i) {
            ApplicationInfo data = applist.get(arr[i]);
            prefapps = prefapps + data.loadLabel(packageManager) + ";";
        }
        sc = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        Editor ed = sc.edit();
        ed.putString("app_loc", prefapps);
        ed.putInt("fff", count);
        ed.commit();

    }

    @Override
    protected void onListItemClick(ListView l,View v,int position,long id){
        super.onListItemClick(l,v,position,id);
        ivlock=(ImageView)v.findViewById(R.id.app_lockimg);
        if(ivlock.getDrawable()!=null){
            ivlock.setImageDrawable(null);
            appflag[position]=false;
        }else{
            ivlock.setImageResource(R.drawable.ic_action_secure);
            appflag[position]=true;
        }
    }
    private List<ApplicationInfo> checkForLaunchIntent(List<ApplicationInfo>list){
        ArrayList<ApplicationInfo> applist=new ArrayList<ApplicationInfo>();
        for(ApplicationInfo info : list){
            try{
                if(null !=packageManager.getLaunchIntentForPackage(info.packageName)){
                    applist.add(info);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return applist;
    }
    private class LoadApplications extends AsyncTask<Void, Void, Void>{
        private ProgressDialog progress = null;

        @Override
        protected Void doInBackground(Void ... params){
            applist=checkForLaunchIntent(packageManager.getInstalledApplications(packageManager.GET_META_DATA));
            listadaptor = new ApplicationAdapter(MainActivity.this,R.layout.snippet_list_row,applist);
            appflag=new boolean[listadaptor.getCount()];
            for(int i = 0;i <listadaptor.getCount();i++){
                appflag[i]=false;
            }
            return null;
        }
        @Override
        protected void onCancelled(){
            super.onCancelled();
        }

        @Override
        protected void onPostExecute( Void result){
            setListAdapter(listadaptor);
            progress.dismiss();
            super.onPostExecute(result);

        }

        @Override
        protected void onPreExecute(){
            progress = ProgressDialog.show( MainActivity.this,null,"Loading application info..");
                    super.onPreExecute();
        }
        @Override
        protected void onProgressUpdate(Void... values){

            super.onProgressUpdate(values);
        }
    }
    public class ApplicationAdapter extends ArrayAdapter<ApplicationInfo>{
        private List<ApplicationInfo> appsList=null;
        private Context context;
        private PackageManager packageManager;

        public ApplicationAdapter(Context context,int textViewResourceId,List<ApplicationInfo>appsList){
            super(context,textViewResourceId,appsList);
        this.context=context;
        this.appsList=appsList;
        packageManager=context.getPackageManager();
        }
         @Override
          public int getCount(){
            return((null!=appsList) ? appsList.size():0);

        }
         @Override
         public ApplicationInfo getItem(int position){
            return((null!=appsList) ? appsList.get(position):null);
         }
        @Override
        public long getItemId(int position){
             return position;
    }
         @Override
         public View getView(int position,View convertView, ViewGroup parent){
        View view = convertView;
        if(null==view) {
            LayoutInflater layoutInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.snippet_list_row, null);
        }
        ApplicationInfo data = appsList.get(position);
            TextView appName = (TextView)view.findViewById(R.id.app_name);
            ImageView iconview=(ImageView)view.findViewById(R.id.app_icon);
            ImageView lockimg=(ImageView)view.findViewById(R.id.app_lockimg);
            String[] prefapps;
            sc=getSharedPreferences(getPackageName(),MODE_PRIVATE);
            int counter=sc.getInt("fff",0);
            prefapps=sc.getString("app_loc","").split(";");
            boolean flag=false;
            String label=(String)data.loadLabel(packageManager);

            for(int i=0;i<prefapps.length;++i)
            {
                if(prefapps[i].equals(label))
                {
                    flag=true;
                }
            }
            if(flag==true)
            {
                lockimg.setImageResource(R.drawable.ic_action_secure);
                appflag[position]=true;
                appName.setText(data.loadLabel(packageManager));
                iconview.setImageDrawable(data.loadIcon(packageManager));

            }
            else{
                lockimg.setImageDrawable(null);
                appflag[position]=false;
                appName.setText(data.loadLabel(packageManager));
                iconview.setImageDrawable(data.loadIcon(packageManager));
            }
            return view;
        }
    }
}
