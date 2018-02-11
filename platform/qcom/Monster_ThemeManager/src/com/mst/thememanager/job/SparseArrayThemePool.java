package com.mst.thememanager.job;

import org.w3c.dom.Node;

import com.mst.thememanager.entities.Theme;

import android.util.Pools.Pool;
import android.util.Pools.SimplePool;
import android.util.SparseArray;

public class SparseArrayThemePool {

    private int mCapacityBytes;
    private SparseArray<Node> mStore = new SparseArray<Node>();
    private int mSizeBytes = 0;

    private Pool<Node> mNodePool;
    private Node mPoolNodesHead = null;
    private Node mPoolNodesTail = null;

    protected static class Node {
        Theme theme;

        // Each node is part of two doubly linked lists:
        // - A pool-level list (accessed by mPoolNodesHead and mPoolNodesTail)
        //   that is used for FIFO eviction of nodes when the pool gets full.
        // - A bucket-level list for each index of the sparse array, so that
        //   each index can store more than one item.
        Node prevInBucket;
        Node nextInBucket;
        Node nextInPool;
        Node prevInPool;
    }

    
    /**
     * @param capacityBytes Maximum capacity of the pool in bytes.
     * @param nodePool Shared pool to use for recycling linked list nodes, or null.
     */
    public SparseArrayThemePool(int capacityBytes, Pool<Node> nodePool) {
		// TODO Auto-generated constructor stub
    	mCapacityBytes = capacityBytes;
        if (nodePool == null) {
            mNodePool = new SimplePool<Node>(32);
        } else {
            mNodePool = nodePool;
        }
	}

	/**
     * Set the maximum capacity of the pool, and if necessary trim it down to size.
     */
    public synchronized void setCapacity(int capacityBytes) {
        mCapacityBytes = capacityBytes;

        // No-op unless current size exceeds the new capacity.
        freeUpCapacity(0);
    }

    private void freeUpCapacity(int bytesNeeded) {
        int targetSize = mCapacityBytes - bytesNeeded;
        // Repeatedly remove the oldest node until we have freed up at least bytesNeeded.
        while (mPoolNodesTail != null && mSizeBytes > targetSize) {
            unlinkAndRecycleNode(mPoolNodesTail, true);
        }
    }

    private void unlinkAndRecycleNode(Node n, boolean recycleTheme) {
        // Unlink the node from its sparse array bucket list.
        if (n.prevInBucket != null) {
            // This wasn't the head, update the previous node.
            n.prevInBucket.nextInBucket = n.nextInBucket;
        } else {
            // This was the head of the bucket, replace it with the next node.
            mStore.put(n.theme.id, n.nextInBucket);
        }
        if (n.nextInBucket != null) {
            // This wasn't the tail, update the next node.
            n.nextInBucket.prevInBucket = n.prevInBucket;
        }

        // Unlink the node from the pool-wide list.
        if (n.prevInPool != null) {
            // This wasn't the head, update the previous node.
            n.prevInPool.nextInPool = n.nextInPool;
        } else {
            // This was the head of the pool-wide list, update the head pointer.
            mPoolNodesHead = n.nextInPool;
        }
        if (n.nextInPool != null) {
            // This wasn't the tail, update the next node.
            n.nextInPool.prevInPool = n.prevInPool;
        } else {
            // This was the tail, update the tail pointer.
            mPoolNodesTail = n.prevInPool;
        }

        // Recycle the node.
        n.nextInBucket = null;
        n.nextInPool = null;
        n.prevInBucket = null;
        n.prevInPool = null;
        mSizeBytes -= n.theme.sizeCount;
        if (recycleTheme) n.theme = null;
        mNodePool.release(n);
    }

    /**
     * @return Capacity of the pool in bytes.
     */
    public synchronized int getCapacity() {
        return mCapacityBytes;
    }

    /**
     * @return Total size in bytes of the themes stored in the pool.
     */
    public synchronized int getSize() {
        return mSizeBytes;
    }

    public synchronized Theme get(int themeId) {
        Node cur = mStore.get(themeId);

        // Traverse the list corresponding to the width bucket in the
        // sparse array, and unlink and return the first theme that
        // also has the correct height.
        while (cur != null) {
            if (cur.theme.id == themeId) {
                Theme  t = cur.theme;
                unlinkAndRecycleNode(cur, false);
                return t;
            }
            cur = cur.nextInBucket;
        }
        return null;
    }

    /**
     * Adds the given theme to the pool.
     * @return Whether the theme was added to the pool.
     */
    public synchronized boolean put(Theme t) {
        if (t == null) {
            return false;
        }

        // Ensure there is enough room to contain the new theme.
        int bytes = t.sizeCount;
        freeUpCapacity(bytes);

        Node newNode = mNodePool.acquire();
        if (newNode == null) {
            newNode = new Node();
        }
        newNode.theme = t;

        // We append to the head, and freeUpCapacity clears from the tail,
        // resulting in FIFO eviction.
        newNode.prevInBucket = null;
        newNode.prevInPool = null;
        newNode.nextInPool = mPoolNodesHead;
        mPoolNodesHead = newNode;

        // Insert the node into its appropriate bucket based on width.
        int key = t.id;
        newNode.nextInBucket = mStore.get(key);
        if (newNode.nextInBucket != null) {
            // The bucket already had nodes, update the old head.
            newNode.nextInBucket.prevInBucket = newNode;
        }
        mStore.put(key, newNode);

        if (newNode.nextInPool == null) {
            // This is the only node in the list, update the tail pointer.
            mPoolNodesTail = newNode;
        } else {
            newNode.nextInPool.prevInPool = newNode;
        }
        mSizeBytes += bytes;
        return true;
    }

    /**
     * Empty the pool, recycling all the themes currently in it.
     */
    public synchronized void clear() {
        // Clearing is equivalent to ensuring all the capacity is available.
        freeUpCapacity(mCapacityBytes);
    }
}
