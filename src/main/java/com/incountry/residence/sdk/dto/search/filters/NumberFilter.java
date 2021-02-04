package com.incountry.residence.sdk.dto.search.filters;

import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NumberFilter extends Filter {

    private static final Logger LOG = LogManager.getLogger(NumberFilter.class);

    private static final String MSG_ERR_NULL_NUMBER_FILTER = "Number filter or it's values can't be null";
    private static final String MSG_ERR_OPERATOR_NON_RANGE = "Operator in non range number filter can by only in [NULL,$not,$lt,$lte,$gt,$gte]";
    private static final String MSG_ERR_OPERATOR_IN_LIST = "Operator in list number filter can by only in [NULL,$not]";


    private static final String MSG_ERR_UNEXPECTED = "Unexpected error";

//    private static List<String> sRangeNumberFilterLeftOperators = new ArrayList<>();
//    private static List<String> sRangeNumberFilterRightOperators = new ArrayList<>();
//    private static List<String> sNumberFilterOperators = new ArrayList<>();
//    private static List<String> sNumberSingleValueFilterOperators = new ArrayList<>();

    private static List<String> sAllOperators = new ArrayList<>();
    private static List<String> sMultipleValueOperators = new ArrayList<>();


    private List<Long> values = new ArrayList<>();
    private String operator;
//    private String operator2;


    static {
        sAllOperators.add(null);
        sAllOperators.add(OPERATOR_GREATER);
        sAllOperators.add(OPERATOR_GREATER_OR_EQUALS);
        sAllOperators.add(OPERATOR_LESS);
        sAllOperators.add(OPERATOR_LESS_OR_EQUALS);
        sAllOperators.add(OPERATOR_NOT);

        sMultipleValueOperators.add(null);
        sMultipleValueOperators.add(OPERATOR_NOT);

//        sRangeNumberFilterLeftOperators.add(OPERATOR_GREATER);
//        sRangeNumberFilterLeftOperators.add(OPERATOR_GREATER_OR_EQUALS);
//
//        sRangeNumberFilterRightOperators.add(OPERATOR_LESS);
//        sRangeNumberFilterRightOperators.add(OPERATOR_LESS_OR_EQUALS);
//
//        sNumberFilterOperators.add(null);
//        sNumberFilterOperators.add(OPERATOR_NOT);
//        sNumberFilterOperators.add(OPERATOR_GREATER);
//        sNumberFilterOperators.add(OPERATOR_GREATER_OR_EQUALS);
//        sNumberFilterOperators.add(OPERATOR_LESS);
//        sNumberFilterOperators.add(OPERATOR_LESS_OR_EQUALS);
//
//        sNumberSingleValueFilterOperators.add(null);
//        sNumberSingleValueFilterOperators.add(OPERATOR_NOT);
    }

    public NumberFilter(List<Long> values) throws StorageClientException {
        this(values, null);
    }

    public NumberFilter(List<Long> values, String operator) throws StorageClientException {
        validate(values, operator);
        this.values.addAll(values);
        this.operator = operator;
    }

    private static void validate(List<Long> values, String operator) throws StorageClientException {
        if (values == null || values.size() == 0) {
            LOG.error(MSG_ERR_NULL_NUMBER_FILTER);
            throw new StorageClientException(MSG_ERR_NULL_NUMBER_FILTER);
        }
        if (!sAllOperators.contains(operator)) {
            LOG.error(MSG_ERR_OPERATOR_NON_RANGE);
            throw new StorageClientException(MSG_ERR_OPERATOR_NON_RANGE);
        }
        if (!sMultipleValueOperators.contains(operator) && values.size() > 1) {
            LOG.error(MSG_ERR_OPERATOR_IN_LIST);
            throw new StorageClientException(MSG_ERR_OPERATOR_IN_LIST);
        }
    }

    @Override
    public Object toTransferObject() {
        if (operator == null) {
            return values;
        }
        Map<Object, Object> transferObject = new HashMap<>();
        transferObject.put(operator, values);
        return transferObject;
//        return new HashMap<Object, Object>() {{ put(operator, values); }};
    }

}
//    //    public override object ToTransferObject()
////    {
////        if (Operator == null)
////        {
////            return _values;
////        }
////
////        return new Dictionary<object, object> { [Operator] = _values };
////    }
//
////    public NumberFilter(String someOperator, long value) {
////        this.values.add(value);
////        this.operator1 = someOperator;
////    }
////
////    public NumberFilter(String operator1, long value1, String operator2, long value2) {
////        this.values.add(value1);
////        this.values.add(value2);
////        this.operator1 = operator1;
////        this.operator2 = operator2;
////    }
//
//    @Override
//    public Filter copy() {
//        return new NumberFilter(values, operator1, operator2);
//    }
//
//    @Override
//    public String toJson() {
//        if (operator2 != null)
//        {
//            return "{\"" + operator1 + "\":" + values.get(0) + ",\"" + operator2 + "\":" + values.get(1) + "}";
//        }
//
//        if (operator1 != null)
//        {
//            return "{\"" + operator1 + "\":[" + values.stream().map(String::valueOf).collect(Collectors.joining(",")) + "]}";
//        }
//
//        return "[" + values.stream().map(String::valueOf).collect(Collectors.joining(",")) + "]";
//    }
//
//    public static void validateNumberFilter(NumberFilter numberFilter) throws StorageClientException {
//        if (numberFilter == null) {
//            LOG.error(MSG_ERR_NULL_NUMBER_FILTER);
//            throw new StorageClientException(MSG_ERR_NULL_NUMBER_FILTER);
//        }
//        if (numberFilter.getValues().size() == 0) {
//            LOG.error(MSG_ERR_NULL_NUMBER_FILTER);
//            throw new StorageClientException(MSG_ERR_NULL_NUMBER_FILTER);
//        }
//        if (numberFilter.getOperator1() != null && numberFilter.getOperator2() != null && numberFilter.getValues().size() == 2) {
//            if(!sRangeNumberFilterLeftOperators.contains(numberFilter.getOperator1())) {
//                LOG.error(MSG_ERR_OPERATOR1_RANGE);
//                throw new StorageClientException(MSG_ERR_OPERATOR1_RANGE);
//            }
//            if(!sRangeNumberFilterRightOperators.contains(numberFilter.getOperator2())) {
//                LOG.error(MSG_ERR_OPERATOR2_RANGE);
//                throw new StorageClientException(MSG_ERR_OPERATOR2_RANGE);
//            }
//            if(numberFilter.values.get(0) > numberFilter.values.get(1)) {
//                LOG.error(MSG_ERR_FIRST_VALUE_MORE_THEN_SECOND_VALUE);
//                throw new StorageClientException(MSG_ERR_FIRST_VALUE_MORE_THEN_SECOND_VALUE);
//            }
//            return;
//        }
//        if (numberFilter.getOperator1() != null && numberFilter.getOperator2() == null) {
//            if (numberFilter.getValues().size() == 1) {
//                if (!sNumberFilterOperators.contains(numberFilter.getOperator1())) {
//                    LOG.error(MSG_ERR_OPERATOR_NON_RANGE);
//                    throw new StorageClientException(MSG_ERR_OPERATOR_NON_RANGE);
//                }
//            } else {
//                if (!sNumberSingleValueFilterOperators.contains(numberFilter.getOperator1())) {
//                    LOG.error(MSG_ERR_OPERATOR_IN_LIST);
//                    throw new StorageClientException(MSG_ERR_OPERATOR_IN_LIST);
//                }
//            }
//            return;
//        }
//        if (numberFilter.getOperator1() == null && numberFilter.getOperator2() == null) {
//            return;
//        }
//        LOG.error(MSG_ERR_UNEXPECTED);
//        throw new StorageClientException(MSG_ERR_UNEXPECTED);
//    }
//
//    public List<Long> getValues() {
//        return values;
//    }
//
//    public String getOperator1() {
//        return operator1;
//    }
//
//    public String getOperator2() {
//        return operator2;
//    }
//
//    @Override
//    public String toString() {
//        return "FilterRangeParam{" +
//                "values=" + values +
//                ", operator1='" + operator1 + '\'' +
//                ", operator2='" + operator2 + '\'' +
//                '}';
//    }
//}












































//package com.incountry.residence.sdk.dto.search;
//
//import com.incountry.residence.sdk.tools.exceptions.StorageClientException;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.stream.Collectors;
//
//public class NumberFilter extends Filter {
//
//    private static final Logger LOG = LogManager.getLogger(NumberFilter.class);
//
//    private static final String MSG_ERR_NULL_NUMBER_FILTER = "Number filter or it's values can't be null";
//    private static final String MSG_ERR_OPERATOR1_RANGE = "Operator1 in range number filter can by only in [$gt,$gte]";
//    private static final String MSG_ERR_OPERATOR2_RANGE = "Operator2 in range number filter can by only in [$lt,$lte]";
//    private static final String MSG_ERR_FIRST_VALUE_MORE_THEN_SECOND_VALUE = "The first value in range filter can by only less or equals the second value";
//    private static final String MSG_ERR_OPERATOR_NON_RANGE = "Operator in non range number filter can by only in [NULL,$not,$lt,$lte,$gt,$gte]";
//    private static final String MSG_ERR_OPERATOR_IN_LIST = "Operator in list number filter can by only in [NULL,$not]";
//    private static final String MSG_ERR_UNEXPECTED = "Unexpected error";
//
//    private static List<String> sRangeNumberFilterLeftOperators = new ArrayList<>();
//    private static List<String> sRangeNumberFilterRightOperators = new ArrayList<>();
//    private static List<String> sNumberFilterOperators = new ArrayList<>();
//    private static List<String> sNumberSingleValueFilterOperators = new ArrayList<>();
//
//    private List<Long> values = new ArrayList<>();
//    private String operator;
//    private String operator2;
//
//
//    static {
//
//        sRangeNumberFilterLeftOperators.add(OPERATOR_GREATER);
//        sRangeNumberFilterLeftOperators.add(OPERATOR_GREATER_OR_EQUALS);
//
//        sRangeNumberFilterRightOperators.add(OPERATOR_LESS);
//        sRangeNumberFilterRightOperators.add(OPERATOR_LESS_OR_EQUALS);
//
//        sNumberFilterOperators.add(null);
//        sNumberFilterOperators.add(OPERATOR_NOT);
//        sNumberFilterOperators.add(OPERATOR_GREATER);
//        sNumberFilterOperators.add(OPERATOR_GREATER_OR_EQUALS);
//        sNumberFilterOperators.add(OPERATOR_LESS);
//        sNumberFilterOperators.add(OPERATOR_LESS_OR_EQUALS);
//
//        sNumberSingleValueFilterOperators.add(null);
//        sNumberSingleValueFilterOperators.add(OPERATOR_NOT);
//    }
//
//    public NumberFilter(List<Long> values, String operator1, String operator2) {
//        if (values != null) {
//            this.values = new ArrayList<>(values);
//        }
//        this.operator1 = operator1;
//        this.operator2 = operator2;
//    }
//
//    public NumberFilter(String someOperator, long value) {
//        this.values.add(value);
//        this.operator1 = someOperator;
//    }
//
//    public NumberFilter(String operator1, long value1, String operator2, long value2) {
//        this.values.add(value1);
//        this.values.add(value2);
//        this.operator1 = operator1;
//        this.operator2 = operator2;
//    }
//
//    @Override
//    public Filter copy() {
//        return new NumberFilter(values, operator1, operator2);
//    }
//
//    @Override
//    public String toJson() {
//        if (operator2 != null)
//        {
//            return "{\"" + operator1 + "\":" + values.get(0) + ",\"" + operator2 + "\":" + values.get(1) + "}";
//        }
//
//        if (operator1 != null)
//        {
//            return "{\"" + operator1 + "\":[" + values.stream().map(String::valueOf).collect(Collectors.joining(",")) + "]}";
//        }
//
//        return "[" + values.stream().map(String::valueOf).collect(Collectors.joining(",")) + "]";
//    }
//
//    public static void validateNumberFilter(NumberFilter numberFilter) throws StorageClientException {
//        if (numberFilter == null) {
//            LOG.error(MSG_ERR_NULL_NUMBER_FILTER);
//            throw new StorageClientException(MSG_ERR_NULL_NUMBER_FILTER);
//        }
//        if (numberFilter.getValues().size() == 0) {
//            LOG.error(MSG_ERR_NULL_NUMBER_FILTER);
//            throw new StorageClientException(MSG_ERR_NULL_NUMBER_FILTER);
//        }
//        if (numberFilter.getOperator1() != null && numberFilter.getOperator2() != null && numberFilter.getValues().size() == 2) {
//            if(!sRangeNumberFilterLeftOperators.contains(numberFilter.getOperator1())) {
//                LOG.error(MSG_ERR_OPERATOR1_RANGE);
//                throw new StorageClientException(MSG_ERR_OPERATOR1_RANGE);
//            }
//            if(!sRangeNumberFilterRightOperators.contains(numberFilter.getOperator2())) {
//                LOG.error(MSG_ERR_OPERATOR2_RANGE);
//                throw new StorageClientException(MSG_ERR_OPERATOR2_RANGE);
//            }
//            if(numberFilter.values.get(0) > numberFilter.values.get(1)) {
//                LOG.error(MSG_ERR_FIRST_VALUE_MORE_THEN_SECOND_VALUE);
//                throw new StorageClientException(MSG_ERR_FIRST_VALUE_MORE_THEN_SECOND_VALUE);
//            }
//            return;
//        }
//        if (numberFilter.getOperator1() != null && numberFilter.getOperator2() == null) {
//            if (numberFilter.getValues().size() == 1) {
//                if (!sNumberFilterOperators.contains(numberFilter.getOperator1())) {
//                    LOG.error(MSG_ERR_OPERATOR_NON_RANGE);
//                    throw new StorageClientException(MSG_ERR_OPERATOR_NON_RANGE);
//                }
//            } else {
//                if (!sNumberSingleValueFilterOperators.contains(numberFilter.getOperator1())) {
//                    LOG.error(MSG_ERR_OPERATOR_IN_LIST);
//                    throw new StorageClientException(MSG_ERR_OPERATOR_IN_LIST);
//                }
//            }
//            return;
//        }
//        if (numberFilter.getOperator1() == null && numberFilter.getOperator2() == null) {
//            return;
//        }
//        LOG.error(MSG_ERR_UNEXPECTED);
//        throw new StorageClientException(MSG_ERR_UNEXPECTED);
//    }
//
//    public List<Long> getValues() {
//        return values;
//    }
//
//    public String getOperator1() {
//        return operator1;
//    }
//
//    public String getOperator2() {
//        return operator2;
//    }
//
//    @Override
//    public String toString() {
//        return "FilterRangeParam{" +
//                "values=" + values +
//                ", operator1='" + operator1 + '\'' +
//                ", operator2='" + operator2 + '\'' +
//                '}';
//    }
//}
