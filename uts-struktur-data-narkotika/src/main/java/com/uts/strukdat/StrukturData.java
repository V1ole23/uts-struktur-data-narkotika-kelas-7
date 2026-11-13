package com.uts.strukdat;

public interface StrukturData<T> {
    void add(T value);
    T get(int position);
    T remove(int position);
    int size();
    boolean isEmpty();
    boolean contains(T value);
    void clear();
}