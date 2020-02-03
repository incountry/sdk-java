package com.incountry.response;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Metadata {
    private int total;
    private int count;
    private int migrated;
    private int totalLeft;
}
