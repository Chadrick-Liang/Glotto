package com.example.Glotto.Orginal_SRS;

import java.util.ArrayList;
import java.util.Comparator;

public class PriorityQueue {
    ArrayList<Flashcard> A;
    int length;
    int heapSize;
    Comparator<Flashcard> comparator;

    PriorityQueue(Comparator<Flashcard> comparator) {
        A = new ArrayList<>();
        heapSize = 0;
        this.comparator = comparator;
    }

    PriorityQueue(ArrayList<Flashcard> a, Comparator<Flashcard> comparator) {
        A = a;
        heapSize = A.size();
        this.comparator = comparator;
        this.minHeapify(0);
    }

    public int getLeft(int i) {
        return (i*2) + 1;
    }
    public int getRight(int i) {
        return (i+1) * 2;
    }
    public int getParent(int i) {
        return (int) Math.floor((i - 1) / 2.0);
    }
    public void swap(int i, int j) {
        Flashcard temp = A.get(i);
        A.set(i, A.get(j));
        A.set(j, temp);
    }

    public void buildMinHeap() {
        int mid = (int) Math.floor(heapSize/2.0);
        for (int i=mid; i>=0; i--) {
            minHeapify(i);
        }
    }

    public void minHeapify(int i) {
        int l = getLeft(i);
        int r = getRight(i);
        int smallest = 0;
        // if left child is smaller than current node
        if ( (l<heapSize) && (comparator.compare(A.get(l),A.get(i))<0) ) {
            smallest = l;
        } else { // if current node is smaller than left child
            smallest = i;
        }
        // if right child is smaller than current node
        if ( (r<heapSize) && (comparator.compare(A.get(r),A.get(smallest))<0) ) {
            smallest = r;
        }
        // if current node is not the smallest then swap
        if (smallest != i) {
            this.swap(i, smallest);
            minHeapify(smallest);
        }
    }

    public void insert(Flashcard x) {
        // insert at the end of the heap
        A.add(x);
        heapSize++;
        // maintain min heap structure
        int i = heapSize - 1;
        int parent = getParent(i);
        while ( (i>1) && (comparator.compare(A.get(i),A.get(parent))<0) ) {
            this.swap(i, parent);
            i = parent;
            parent = getParent(i);
        }
    }

    public Flashcard extract_min() {
//        // if queue is empty
//        if (A.isEmpty()) {
//            ;
//        }
        // remove the root node and replace it with the last node
        this.swap(0, length-1);
        Flashcard min = A.get(length-1);
        length--;
        // re-heapify the min heap
        this.minHeapify(0);
        return min;
    }

    public Flashcard get_min() {
        // if queue is empty
        if (A.isEmpty()) {
            return null;
        }
        return A.get(0);
    }

    public boolean isEmpty() {
        return A.isEmpty();
    }
}

