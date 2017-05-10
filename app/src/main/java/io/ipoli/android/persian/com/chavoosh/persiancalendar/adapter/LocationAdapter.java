//package io.ipoli.android.persian.com.chavoosh.persiancalendar.adapter;
//
//import android.content.Context;
//import android.support.v7.widget.RecyclerView;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//
//
//
//import java.util.List;
//
//import io.ipoli.android.R;
//import io.ipoli.android.persian.com.chavoosh.persiancalendar.entity.CityEntity;
//import io.ipoli.android.persian.com.chavoosh.persiancalendar.util.Utils;
//import io.ipoli.android.persian.com.chavoosh.persiancalendar.view.preferences.LocationPreferenceDialog;
//
//public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.ViewHolder> {
//    private String locale;
//    private List<CityEntity> cities;
//    private Utils utils;
//    LocationPreferenceDialog locationPreferenceDialog;
//    LayoutInflater layoutInflater;
//
//    public LocationAdapter(LocationPreferenceDialog locationPreferenceDialog) {
//        Context context = locationPreferenceDialog.getContext();
//        utils = Utils.getInstance(locationPreferenceDialog.getContext());
//        this.layoutInflater = LayoutInflater.from(context);
//        this.locationPreferenceDialog = locationPreferenceDialog;
////        this.cities = utils.getAllCities(true);
//        this.locale = utils.getAppLanguage();
//    }
//
//    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
//        private TextView country;
//        private TextView city;
//
//        public ViewHolder(View itemView) {
//            super(itemView);
//            itemView.setOnClickListener(this);
//            city = (TextView) itemView.findViewById(R.id.text1);
//            country = (TextView) itemView.findViewById(R.id.text2);
//        }
//
//        @Override
//        public void onClick(View view) {
//            locationPreferenceDialog.selectItem(cities.get(getAdapterPosition()).getKey());
//        }
//    }
//
//    @Override
//    public LocationAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        return new ViewHolder(layoutInflater.inflate(R.layout.list_item_city_name, parent, false));
//    }
//
//    @Override
//    public void onBindViewHolder(ViewHolder holder, int position) {
//        utils.setFont(holder.city);
//        holder.city.setText(locale.equals("en")
//                ? cities.get(position).getEn()
//                : utils.shape(cities.get(position).getFa()));
//
//        utils.setFont(holder.country);
//        holder.country.setText(locale.equals("en")
//                ? cities.get(position).getCountryEn()
//                : utils.shape(cities.get(position).getCountryFa()));
//    }
//
//    @Override
//    public int getItemCount() {
//        return cities.size();
//    }
//}