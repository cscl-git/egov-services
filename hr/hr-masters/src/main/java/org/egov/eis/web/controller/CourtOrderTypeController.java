/*
 * eGov suite of products aim to improve the internal efficiency,transparency,
 * accountability and the service delivery of the government  organizations.
 *
 *  Copyright (C) 2016  eGovernments Foundation
 *
 *  The updated version of eGov suite of products as by eGovernments Foundation
 *  is available at http://www.egovernments.org
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see http://www.gnu.org/licenses/ or
 *  http://www.gnu.org/licenses/gpl.html .
 *
 *  In addition to the terms of the GPL license to be adhered to in using this
 *  program, the following additional terms are to be complied with:
 *
 *      1) All versions of this program, verbatim or modified must carry this
 *         Legal Notice.
 *
 *      2) Any misrepresentation of the origin of the material is prohibited. It
 *         is required that all modified versions of this material be marked in
 *         reasonable ways as different from the original version.
 *
 *      3) This license does not grant any rights to any user of the program
 *         with regards to rights under trademark law for use of the trade names
 *         or trademarks of eGovernments Foundation.
 *
 *  In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
 */

package org.egov.eis.web.controller;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.eis.model.CourtOrderType;
import org.egov.eis.service.CourtOrderTypeService;
import org.egov.eis.web.contract.CourtOrderTypeGetRequest;
import org.egov.eis.web.contract.CourtOrderTypeResponse;
import org.egov.eis.web.contract.RequestInfoWrapper;
import org.egov.eis.web.contract.factory.ResponseInfoFactory;
import org.egov.eis.web.errorhandlers.ErrorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/courtordertype")
public class CourtOrderTypeController {

    private static final Logger logger = LoggerFactory.getLogger(CourtOrderTypeController.class);

    @Autowired
    private CourtOrderTypeService courtOrderTypeService;

    @Autowired
    private ErrorHandler errHandler;

    @Autowired
    private ResponseInfoFactory responseInfoFactory;


    @PostMapping("/_search")
    @ResponseBody
    public ResponseEntity<?> search(@ModelAttribute @Valid CourtOrderTypeGetRequest courtOrderTypeGetRequest,
                                    BindingResult modelAttributeBindingResult, @RequestBody @Valid RequestInfoWrapper requestInfoWrapper,
                                    BindingResult requestBodyBindingResult) {
        RequestInfo requestInfo = requestInfoWrapper.getRequestInfo();

        // validate input params
        if (modelAttributeBindingResult.hasErrors()) {
            return errHandler.getErrorResponseEntityForMissingParameters(modelAttributeBindingResult, requestInfo);
        }

        // validate input params
        if (requestBodyBindingResult.hasErrors()) {
            return errHandler.getErrorResponseEntityForMissingRequestInfo(requestBodyBindingResult, requestInfo);
        }
        // Call service
        List<CourtOrderType> courtOrderTypeList = null;
        try {
            courtOrderTypeList = courtOrderTypeService.getCourtOrderTypes(courtOrderTypeGetRequest);
        } catch (final Exception exception) {
            logger.error("Error while processing request " + courtOrderTypeGetRequest, exception);
            return errHandler.getResponseEntityForUnexpectedErrors(requestInfo);
        }

        return getSuccessResponse(courtOrderTypeList, requestInfo);
    }

    /**
     * Populate Response object and courtOrderTypelist
     *
     * @param courtOrderTypeList
     * @return
     */
    private ResponseEntity<?> getSuccessResponse(final List<CourtOrderType> courtOrderTypeList,
                                                 final RequestInfo requestInfo) {
        final CourtOrderTypeResponse courtOrderTypeRes = new CourtOrderTypeResponse();
        courtOrderTypeRes.setCourtOrderType(courtOrderTypeList);
        final ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(requestInfo, true);
        responseInfo.setStatus(HttpStatus.OK.toString());
        courtOrderTypeRes.setResponseInfo(responseInfo);
        return new ResponseEntity<CourtOrderTypeResponse>(courtOrderTypeRes, HttpStatus.OK);

    }

}
