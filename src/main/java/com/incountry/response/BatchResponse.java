package com.incountry.response;

import com.incountry.BatchRecord;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BatchResponse {
    private BatchRecord batchRecord;
    private Metadata meta;
}
