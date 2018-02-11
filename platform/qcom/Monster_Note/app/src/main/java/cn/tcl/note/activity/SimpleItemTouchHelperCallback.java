/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.note.activity;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

/**
 * handle item move
 */
public class SimpleItemTouchHelperCallback extends ItemTouchHelper.Callback {
    private onMoveListener moveListener;

    public SimpleItemTouchHelperCallback(onMoveListener moveListener) {
        this.moveListener = moveListener;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        if (viewHolder instanceof NoteEditorAdapter.NoteTextView) {
            return 0;
        }
        int dragFlag = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        return makeMovementFlags(dragFlag, 0);

    }

    @Override
    public boolean isLongPressDragEnabled() {
        return false;
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        if (viewHolder instanceof NoteEditorAdapter.NoteTextView) {
            return false;
        }
        moveListener.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
            if (viewHolder instanceof NoteEditorAdapter.NoteAttachView) {
                ((onPicMoveListener) viewHolder).onItemSelected();
            }
        }
        super.onSelectedChanged(viewHolder, actionState);
    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        if (viewHolder instanceof NoteEditorAdapter.NoteAttachView) {
            ((onPicMoveListener) viewHolder).onItemClear();
        }
    }

    public interface onPicMoveListener {
        void onItemSelected();

        void onItemClear();
    }

    public interface onMoveListener {
        boolean onItemMove(int from, int to);
    }
}
