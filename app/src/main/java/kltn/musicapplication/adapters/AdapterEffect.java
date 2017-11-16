package kltn.musicapplication.adapters;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import kltn.musicapplication.ConnectActivity;
import kltn.musicapplication.R;
import kltn.musicapplication.animations.RecyclerViewAnimator;
import kltn.musicapplication.models.Bluetooth;
import kltn.musicapplication.models.Effect;


public class AdapterEffect extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private LayoutInflater mInflater;
    private ArrayList<Effect> items;
    private Activity mActivity;
    private Bluetooth bluetooth;
    private BluetoothDevice device;
    private RecyclerViewAnimator recyclerViewAnimator;

    public AdapterEffect(ArrayList<Effect> data, ConnectActivity activity, Bluetooth bluetooth, BluetoothDevice device, RecyclerView recyclerView) {
        this.items = data;
        this.mActivity = activity;
        this.bluetooth = bluetooth;
        this.device = device;
        recyclerViewAnimator = new RecyclerViewAnimator(recyclerView);
        this.mInflater = LayoutInflater.from(mActivity);
    }

    public void addItem(Effect result) {
        items.add(result);
    }

    public void setInflater(LayoutInflater layoutInflater){
        this.mInflater = layoutInflater;
    }

    public void replaceItems(ArrayList<Effect> newItems) {
        this.items.clear();
        for(Effect item: newItems)
            this.items.add(item);
    }

    public void insertItem(Effect item) {
        items.add(item);
    }

    public void clearItems(){
        items.clear();
    }

    public void AddResults(ArrayList<Effect> result) {
        items.addAll(result);
    }

    public Effect getItemsAt(int position){
        return  items.get(position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Effect model = items.get(position);
        MessageViewHolder messageViewHolder = (MessageViewHolder) holder;
        messageViewHolder.imageView_logo.setBackgroundResource(model.getImage());
        messageViewHolder.textView_title.setText(model.getTitle());
        messageViewHolder.textView_code.setText("Code: " + model.getCode());
        messageViewHolder.textView_content.setText(model.getContent());
        recyclerViewAnimator.onBindViewHolder(holder.itemView, position);
    }

    @Override
    public int getItemViewType(int position) {
        return  super.getItemViewType(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rootCategoryView = mInflater.inflate(R.layout.item_effect_view, parent, false);
        recyclerViewAnimator.onCreateViewHolder(rootCategoryView);
        return new MessageViewHolder(rootCategoryView, this);
    }

    private class MessageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView imageView_logo;
        private TextView textView_title;
        private TextView textView_code;
        private TextView textView_content;
        private CardView cardView_effect;

        private MessageViewHolder(View itemView, AdapterEffect adapter) {
            super(itemView);
            imageView_logo = (ImageView) itemView.findViewById(R.id.img_logo);
            textView_title = (TextView) itemView.findViewById(R.id.txtv_title);
            textView_code = (TextView) itemView.findViewById(R.id.txtv_code);
            textView_content = (TextView) itemView.findViewById(R.id.txtv_content);
            cardView_effect = (CardView) itemView.findViewById(R.id.cardView_effect);

            cardView_effect.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            final int pos = getAdapterPosition();
            Effect effect = items.get(pos);
            if (pos >= 0) {
                switch (bluetooth.getState()){
                    case Bluetooth.STATE_CONNECTED:
                        bluetooth.send(effect.getCode());
                        Toast.makeText(mActivity, "Selected Item Position " + effect.getCode(), Toast.LENGTH_SHORT).show();
                        break;
                    case Bluetooth.STATE_CONNECTING:
                        Snackbar.make(mActivity.findViewById(R.id.coordinator_layout_connect), "Connecting. Please waiting...", Snackbar.LENGTH_SHORT).show();
                        break;
                    case Bluetooth.STATE_ERROR:
                        Snackbar.make(mActivity.findViewById(R.id.coordinator_layout_connect), "Connect error. Please Reconnect !", Snackbar.LENGTH_SHORT).setAction("Reconnect", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mActivity.findViewById(R.id.btn_reconnect).setEnabled(false);
                                bluetooth.disconnect();
                                bluetooth.connectToDevice(device);
                            }
                        }).show();
                        break;
                    case Bluetooth.STATE_NONE:
                        Snackbar.make(mActivity.findViewById(R.id.coordinator_layout_connect), "You are not Connect, Please Connect", Snackbar.LENGTH_SHORT).setAction("Connect", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mActivity.findViewById(R.id.btn_reconnect).setEnabled(false);
                                bluetooth.disconnect();
                                bluetooth.connectToDevice(device);
                            }
                        }).show();
                        break;
                }
            }
        }
    }
}