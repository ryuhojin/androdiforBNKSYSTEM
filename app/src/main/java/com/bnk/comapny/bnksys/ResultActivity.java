package com.bnk.comapny.bnksys;

import android.content.Context;
import android.content.Entity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bnk.comapny.bnksys.model.Apartment;
import com.bnk.comapny.bnksys.model.ApartmentList;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ResultActivity extends AppCompatActivity {
    MapView mapView;
    ListView listView;
    LineChart lineChart;

    ApartmentList tmpAptList;
    Apartment tmpApt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        //지도 관련..
        Intent intent = getIntent();

        Double mapX = Double.parseDouble(intent.getStringExtra("mapX"));
        Double mapY = Double.parseDouble(intent.getStringExtra("mapY"));

        String address = intent.getStringExtra("address");
        int idx = address.indexOf("@");
        String aptName = address.substring(idx + 1);

        Toast.makeText(this, mapX + ", " + mapY, Toast.LENGTH_SHORT).show();

        mapView = findViewById(R.id.result_map_view);
        mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(mapX, mapY), true);

        MapPOIItem marker = new MapPOIItem();
        marker.setMapPoint(MapPoint.mapPointWithGeoCoord(mapX, mapY));
        marker.setItemName(aptName);
        marker.setTag(0);
        marker.setMarkerType(MapPOIItem.MarkerType.RedPin);
        mapView.addPOIItem(marker);


        //리스트 관련..
        address = address.replace("$", "");
        address = address.replace("@", "");

        List<Apartment> aptList = null;
        for(int i = 0; i < LoadingActivity.aptAdressList.size(); i++){
            tmpAptList = LoadingActivity.aptAdressList.get(i);
            if(tmpAptList.getAddress().equals(address)){
                aptList = tmpAptList.getList();
                break;
            }
        }

        ListView listView = findViewById(R.id.result_list);
        AptAdapter aptAdapter = new AptAdapter(this, R.id.result_list, aptList);
        listView.setAdapter(aptAdapter);

        //그래프 관련..
        lineChart = (LineChart) findViewById(R.id.result_graph);
        ArrayList<Entity> values = new ArrayList<>();

        HashSet<Integer> sizeSet = new HashSet(); //평수List
        for(int i = 0; i < aptList.size(); i++){
            sizeSet.add(aptList.get(i).getSizeP());
        }

        List<Integer> sizeList = new ArrayList<>(sizeSet);
        Collections.sort(sizeList); //평수List 오름차순으로 변경

        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(this, R.layout.result_graph_filter, sizeList);
        Spinner filter = findViewById(R.id.result_graph_filter);
        filter.setAdapter(adapter); //평수List를 Spinner 목록에 등록

        List<List<Deal>> dealList = new ArrayList<>(); //평수별 Apartment 정보 list
        for(int i = 0; i < sizeList.size(); i++){
            dealList.add(new ArrayList<Deal>());
        }

        for(int i = 0; i < aptList.size(); i++){ //평수List와 평수별<Apartment>List의 인덱스를 동일하게 구성
            for(int j = 0; j < sizeList.size(); j++){
                tmpApt = aptList.get(i);
                if(tmpApt.getSizeP() == sizeList.get(j)){
                    dealList.get(j).add(new Deal(tmpApt.getContractYM() + tmpApt.getContractD(), tmpApt.getPayout()));
                    break;
                }
            }
        }

        int numData = 5;

        for(int i = 0; i < numData; i++){
        }

//        LineDataSet dataSet = new LineDataSet()

    }

    public class Deal implements Comparable {
        String date;
        int payout;

        public Deal() { }

        public Deal(String date, int payout) {
            if(date.length() == 7){ //2020111과 같은 월에 0이 생략된 경우 예외처리
                String tmp = date.substring(0, 4);
                tmp += "0";
                tmp += date.substring(4);
                date = tmp;
            }
            this.date = date;
            this.payout = payout;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public int getPayout() {
            return payout;
        }

        public void setPayout(int payout) {
            this.payout = payout;
        }

        @Override
        public int compareTo(Object o) {
            return 0;
        }
    }

    public class AptAdapter extends ArrayAdapter<Apartment> {
        private Context context;
        private List<Apartment> list;
        private LayoutInflater inflater = null;



        public AptAdapter(@NonNull Context context, int resource, @NonNull List<Apartment> list) {
            super(context, resource, list);
            this.context = context;
            this.list = list;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Nullable
        @Override
        public Apartment getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public class ViewHolder {
            //이름, 층수, 크기(sizeM)m2, 크기(sizeP)평수, 계약년월일, 매매대금
            public TextView name;
            public TextView size;
            public TextView floor;
            public TextView date;
            public TextView payout;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View row = convertView;
            ViewHolder holder;

            if(row == null){
                LayoutInflater layoutInflater = LayoutInflater.from(context);
                row = layoutInflater.inflate(R.layout.result_list_item, parent, false);

                holder = new ViewHolder();
                holder.name = (TextView)row.findViewById(R.id.result_list_item_name);
                holder.size = (TextView)row.findViewById(R.id.result_list_item_size);
                holder.floor = (TextView)row.findViewById(R.id.result_list_item_floor);
                holder.date = (TextView)row.findViewById(R.id.result_list_item_date);
                holder.payout = (TextView)row.findViewById(R.id.result_list_item_payout);

                row.setTag(holder);

            }else{
                holder = (ViewHolder) row.getTag();
            }

            String Tag = row.getTag().toString();
            int idx = Tag.indexOf("@");
            String tag = Tag.substring(idx + 1);

            Apartment apt = (Apartment) list.get(position);


            holder.name.setText(apt.getName());
            holder.size.setText(apt.getSizeP() + "평(" + apt.getSizeM() + ")");
            holder.floor.setText(apt.getFloor() + "층");
            String tmp = apt.getContractD();
            if(tmp.length() <= 1){
                tmp = "0" + tmp;
            }
            holder.date.setText(apt.getContractYM() + tmp);
            tmp = apt.getPayout() + "";
            if(tmp.length() > 4){
                String billion = tmp.substring(0, (tmp.length() - 4));
                String million = tmp.substring((tmp.length() - 4));
                while(million.length() != 0 && million.charAt(0) == '0'){
                    million = million.replaceFirst("0", "");
                }
                tmp = billion + "억 " + million;
            }
            holder.payout.setText(tmp);

            return row;
        }
    }
}
