package com.milabuda.redditconnector.sourcerecord.transformer;

import com.milabuda.redditconnector.api.model.Listing;
import org.apache.kafka.connect.source.SourceRecord;

import java.util.List;

public interface Transformer<T> {
    List<SourceRecord> transform(Listing<T> listing);
}
