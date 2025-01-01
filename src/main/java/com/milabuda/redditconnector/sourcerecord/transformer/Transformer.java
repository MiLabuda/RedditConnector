package com.milabuda.redditconnector.sourcerecord.transformer;

import com.milabuda.redditconnector.sourcerecord.schema.EventType;
import org.apache.kafka.connect.source.SourceRecord;

import java.util.List;

//Was used as a base for transformers, but i needed more flexibility
//And left it unused until I think of a way to implement it
public interface Transformer<T> {
    List<SourceRecord> transform(List<T> listing);
    List<SourceRecord> transform(List<T> listing, EventType eventType);
}
