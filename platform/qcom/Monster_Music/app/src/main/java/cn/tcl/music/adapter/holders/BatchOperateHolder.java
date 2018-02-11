package cn.tcl.music.adapter.holders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import cn.tcl.music.R;

/**
 * Created by jiangyuanxi on 3/3/16.
 */
public class BatchOperateHolder extends ClickableViewHolder {

    public BatchOperateHolder(View itemView,
                                OnViewHolderClickListener onViewHolderClickListener) {
        super(itemView, onViewHolderClickListener);

        //		shuffleAllBtn = (Button) itemView.findViewById(R.id.shuffle_all_btn);
//		shuffleAllBtn.setOnClickListener(this);
        playAllBtn = (ImageView) itemView.findViewById(R.id.play_all_image);
        playAllBtn.setOnClickListener(this);

        batchPperate = (TextView) itemView.findViewById(R.id.batch_operate);
        batchPperate.setOnClickListener(this);

        /* MODIFIED-BEGIN by beibei.yang, 2016-06-16,BUG-2343725,2203366*/
        playAllText = (TextView) itemView.findViewById(R.id.play_all);
        playAllBtn.setOnClickListener(this);
    }

    //	public Button shuffleAllBtn;
    public ImageView playAllBtn;
    public TextView batchPperate;
    public TextView playAllText;
    /* MODIFIED-END by beibei.yang,BUG-2343725,2203366*/

}
