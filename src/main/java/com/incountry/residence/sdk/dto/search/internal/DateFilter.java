package com.incountry.residence.sdk.dto.search.internal;

import java.util.Date;

public class DateFilter extends Filter {

    private final Date date;

    public DateFilter(Date date) {
        this.date = date;
    }

    @Override
    public Object toTransferObject() {
        return date;
    }
}
