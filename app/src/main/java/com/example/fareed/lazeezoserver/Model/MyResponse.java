package com.example.fareed.lazeezoserver.Model;


import java.util.List;


/**
 * Created by fareed on 2/4/2018.
 */

public class MyResponse {
    public long multicast_id;
    public int success;
    public int failure;
    public int canonical_ids;
    public List<Result> results;
}
