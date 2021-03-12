package com.incountry.residence.sdk.dto.search.internal;

import java.util.Calendar;
import java.util.Date;

public class DateFilter extends Filter {

    private final Date date;

    public DateFilter(Date date) {
        //getting rid of milliseconds
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.MILLISECOND, 0);
        this.date = calendar.getTime();
    }

    @Override
    public Object toTransferObject() {
        return date;
    }
}
