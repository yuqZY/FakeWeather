package com.liyu.suzhoubus.ui.bus;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.baidu.location.BDLocation;
import com.liyu.suzhoubus.R;
import com.liyu.suzhoubus.http.ApiFactory;
import com.liyu.suzhoubus.http.BaseBusResponse;
import com.liyu.suzhoubus.location.RxLocation;
import com.liyu.suzhoubus.model.BusLineNearby;
import com.liyu.suzhoubus.ui.base.BaseContentFragment;
import com.liyu.suzhoubus.ui.bus.adapter.LineNearbyAdapter;
import com.liyu.suzhoubus.ui.bus.adapter.StationNearbyAdapter;

import java.util.HashMap;
import java.util.Map;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by liyu on 2016/10/31.
 */

public class NearbyStationFragment extends BaseContentFragment {

    private RecyclerView recyclerView;
    private StationNearbyAdapter adapter;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_station_nearby;
    }

    @Override
    protected void initViews() {
        super.initViews();
        recyclerView = findView(R.id.rv_line_search);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new StationNearbyAdapter(R.layout.item_bus_line_nearby, null);
        adapter.openLoadAnimation();
        recyclerView.setAdapter(adapter);

    }

    @Override
    protected void lazyFetchData() {
        refreshLayout.setRefreshing(true);
        RxLocation.get().locate(getActivity())
                .flatMap(new Func1<BDLocation, Observable<BaseBusResponse<BusLineNearby>>>() {
                    @Override
                    public Observable<BaseBusResponse<BusLineNearby>> call(BDLocation bdLocation) {
                        Map<String, String> options = new HashMap<>();
                        options.put("lat", String.valueOf(bdLocation.getLatitude()));
                        options.put("lng", String.valueOf(bdLocation.getLongitude()));
                        options.put("more", "1");
                        return ApiFactory.getBusController().getVicinity(options).subscribeOn(Schedulers.io());
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseBusResponse<BusLineNearby>>() {
                    @Override
                    public void onCompleted() {
                        refreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        refreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onNext(BaseBusResponse<BusLineNearby> busLineNearbyBaseBusResponse) {
                        adapter.setNewData(busLineNearbyBaseBusResponse.data.getStation());
                    }
                });
    }

}
