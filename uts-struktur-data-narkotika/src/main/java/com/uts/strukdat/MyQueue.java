package com.uts.strukdat;

public class MyQueue<T> implements StrukturData<T> {

    // Node internal untuk Singly Linked List
    private static class Node<T> {
        T data;
        Node<T> next;

        Node(T data) {
            this.data = data;
            this.next = null;
        }
    }

    private Node<T> head; // Menunjuk ke depan antrean (front)
    private Node<T> tail; // Menunjuk ke belakang antrean (rear)
    private int size;

    public MyQueue() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }

    @Override
    public void add(T value) {
        Node<T> newNode = new Node<>(value);
        if (isEmpty()) {
            head = newNode;
            tail = newNode;
        } else {
            tail.next = newNode;
            tail = newNode;
        }
        size++;
    }

    @Override
    public T get(int position) {
        validateIndex(position);
        Node<T> current = head;
        for (int i = 0; i < position; i++) {
            current = current.next;
        }
        return current.data;
    }

    @Override
    public T remove(int position) {
        validateIndex(position);
        if (position == 0) {
            T removedData = head.data;
            head = head.next;
            if (head == null) {
                tail = null;
            }
            size--;
            return removedData;
        }

        Node<T> prev = head;
        for (int i = 0; i < position - 1; i++) {
            prev = prev.next;
        }

        T removedData = prev.next.data;
        prev.next = prev.next.next;
        if (prev.next == null) {
            tail = prev;
        }
        size--;
        return removedData;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean contains(T value) {
        Node<T> current = head;
        while (current != null) {
            if (value == null ? current.data == null : value.equals(current.data)) {
                return true;
            }
            current = current.next;
        }
        return false;
    }

    @Override
    public void clear() {
        head = null;
        tail = null;
        size = 0;
    }

    private void validateIndex(int position) {
        if (position < 0 || position >= size) {
            throw new IndexOutOfBoundsException("Indeks: " + position + ", Ukuran: " + size);
        }
    }



    // Melakukan pencarian dengan rotasi antrean (rotateSearch) tanpa merusak FIFO,
    // serta mencatat jumlah langkah (steps) dan mengembalikan posisi ketemu (atau -1).
    
    private int searchSteps = 0;
    public int getSearchSteps() {
        return searchSteps;
    }

    
    // Mencari elemen dalam queue dengan memutar antrean.
   //  Mengembalikan true jika ditemukan, dan menyimpan jumlah langkah.

    public boolean rotateSearch(T target) {
        if (isEmpty()) {
            searchSteps = 0;
            return false;
        }

        int n = size;
        boolean found = false;
        searchSteps = 0;

        for (int i = 0; i < n; i++) {
            searchSteps++;
            T current = remove(0); // Ambil elemen depan
            
            if (current != null && current.equals(target)) {
                found = true;
            }
            
            add(current); // Kembalikan ke belakang (rotasi)
            if (found && i == 0) {
                // Jika ditemukan pada langkah pertama, hentikan rotasi lebih lanjut
                break;
            }
        }
        
        return found;
    }
}