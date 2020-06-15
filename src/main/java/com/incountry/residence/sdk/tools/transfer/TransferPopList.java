package com.incountry.residence.sdk.tools.transfer;

import com.incountry.residence.sdk.tools.exceptions.StorageServerException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class TransferPopList {

    private static final Logger LOG = LogManager.getLogger(TransferPopList.class);

    private static final String MSG_ERR_NULL_POPLIST = "Response error: country list is empty";
    private static final String MSG_ERR_NULL_POPNAME = "Response error: country name is empty %s";
    private static final String MSG_ERR_NULL_POPID = "Response error: country id is empty %s";

    List<TransferPop> countries;

    public static void validatePopList(TransferPopList one) throws StorageServerException {
        if (one == null || one.countries == null || one.countries.isEmpty()) {
            LOG.error(MSG_ERR_NULL_POPLIST);
            throw new StorageServerException(MSG_ERR_NULL_POPLIST);
        }
        for (TransferPop pop : one.countries) {
            if (pop.getName() == null || pop.getName().isEmpty()) {
                String message = String.format(MSG_ERR_NULL_POPNAME, pop.toString());
                LOG.error(message);
                throw new StorageServerException(message);
            }
            if (pop.getId() == null || pop.getId().isEmpty()) {
                String message = String.format(MSG_ERR_NULL_POPID, pop.toString());
                LOG.error(message);
                throw new StorageServerException(message);
            }
        }
    }

    public List<TransferPop> getCountries() {
        return countries;
    }
}
