package org.egov.lams.service;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.egov.lams.config.PropertiesManager;
import org.egov.lams.model.Agreement;
import org.egov.lams.model.Demand;
import org.egov.lams.model.DemandDetails;
import org.egov.lams.model.PaymentInfo;
import org.egov.lams.producers.AgreementProducer;
import org.egov.lams.repository.BillRepository;
import org.egov.lams.repository.DemandRepository;
import org.egov.lams.repository.FinancialsRepository;
import org.egov.lams.repository.rowmapper.AgreementRowMapper;
import org.egov.lams.web.contract.AgreementRequest;
import org.egov.lams.web.contract.BillDetailInfo;
import org.egov.lams.web.contract.BillInfo;
import org.egov.lams.web.contract.BillReceiptInfoReq;
import org.egov.lams.web.contract.BillReceiptReq;
import org.egov.lams.web.contract.BillSearchCriteria;
import org.egov.lams.web.contract.BoundaryResponse;
import org.egov.lams.web.contract.ChartOfAccountContract;
import org.egov.lams.web.contract.DemandSearchCriteria;
import org.egov.lams.web.contract.LamsConfigurationGetRequest;
import org.egov.lams.web.contract.ReceiptAccountInfo;
import org.egov.lams.web.contract.ReceiptAmountInfo;
import org.egov.lams.web.contract.RequestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class PaymentService {

	private static final Logger LOGGER = Logger.getLogger(PaymentService.class);

	@Autowired
	PropertiesManager propertiesManager;

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Autowired
	AgreementService agreementService;

	@Autowired
	AgreementProducer agreementProducer;

	@Autowired
	RestTemplate restTemplate;

	@Autowired
	LamsConfigurationService lamsConfigurationService;

	@Autowired
	DemandRepository demandRepository;

	@Autowired
	BillRepository billRepository;

	@Autowired
	BillNumberService billNumberService;

	@Autowired
	FinancialsRepository financialsRepository;

	public String generateBillXml(Agreement agreement, RequestInfo requestInfo) {
		String collectXML = "";
		try {
			LamsConfigurationGetRequest lamsGetRequest = new LamsConfigurationGetRequest();
			List<BillInfo> billInfos = new ArrayList<>();
			BillInfo billInfo = new BillInfo();
			billInfo.setId(null);
			LOGGER.info("the demands for a agreement object" + agreement.getDemands());
			if (agreement.getDemands() != null && !agreement.getDemands().isEmpty()) {
				LOGGER.info("the demand id from agreement object" + agreement.getDemands().get(0));
				billInfo.setDemandId(Long.valueOf(agreement.getDemands().get(0)));
			}
			billInfo.setCitizenName(agreement.getAllottee().getName());
			billInfo.setTenantId(agreement.getTenantId());
			// billInfo.setCitizenAddress(agreement.getAllottee().getAddress());
			// TODO: Fix me after the issue is fixed by user service
			billInfo.setCitizenAddress("Test");
			billInfo.setBillType("AUTO");
			billInfo.setIssuedDate(new Date());
			billInfo.setLastDate(new Date());
			lamsGetRequest.setName("MODULE_NAME");
			LOGGER.info("before moduleName>>>>>>>");

			String moduleName = lamsConfigurationService.getLamsConfigurations(lamsGetRequest).get("MODULE_NAME")
					.get(0);
			LOGGER.info("after moduleName>>>>>>>" + moduleName);
			billInfo.setModuleName(moduleName);
			lamsGetRequest.setTenantId(agreement.getTenantId());
			lamsGetRequest.setName("FUND_CODE");
			String fundCode = lamsConfigurationService.getLamsConfigurations(lamsGetRequest).get("FUND_CODE").get(0);
			billInfo.setFundCode(fundCode);
			LOGGER.info("after fundCode>>>>>>>" + fundCode);

			lamsGetRequest.setName("FUNCTIONARY_CODE");
			String functionaryCode = lamsConfigurationService.getLamsConfigurations(lamsGetRequest)
					.get("FUNCTIONARY_CODE").get(0);
			LOGGER.info("after functionaryCode>>>>>>>" + functionaryCode);

			billInfo.setFunctionaryCode(Long.valueOf(functionaryCode));
			lamsGetRequest.setName("FUNDSOURCE_CODE");
			String fundSourceCode = lamsConfigurationService.getLamsConfigurations(lamsGetRequest)
					.get("FUNDSOURCE_CODE").get(0);
			LOGGER.info("after fundSourceCode>>>>>>>" + fundSourceCode);

			billInfo.setFundSourceCode(fundSourceCode);
			lamsGetRequest.setName("DEPARTMENT_CODE");
			String departmentCode = lamsConfigurationService.getLamsConfigurations(lamsGetRequest)
					.get("DEPARTMENT_CODE").get(0);
			billInfo.setDepartmentCode(departmentCode);
			LOGGER.info("after departmentCode>>>>>>>" + departmentCode);

			billInfo.setCollModesNotAllowed("");
			if (agreement.getAsset().getLocationDetails().getElectionWard() != null) {
				BoundaryResponse boundaryResponse = getBoundariesById(
						agreement.getAsset().getLocationDetails().getElectionWard(),agreement.getTenantId());
				billInfo.setBoundaryNumber(boundaryResponse.getBoundarys().get(0).getBoundaryNum());
				lamsGetRequest.setName("BOUNDARY_TYPE");
				String boundaryType = lamsConfigurationService.getLamsConfigurations(lamsGetRequest)
						.get("BOUNDARY_TYPE").get(0);
				LOGGER.info("after boundaryType>>>>>>>" + boundaryType);

				billInfo.setBoundaryType(boundaryType);
			} else {
				// Passing Admin City boundary details when election ward is not
				// available
				billInfo.setBoundaryType("City");
				billInfo.setBoundaryNumber(1l);
			}
			lamsGetRequest.setName("SERVICE_CODE");
			String serviceCode = lamsConfigurationService.getLamsConfigurations(lamsGetRequest).get("SERVICE_CODE")
					.get(0);
			LOGGER.info("after serviceCode>>>>>>>" + serviceCode);

			billInfo.setServiceCode(serviceCode);
			billInfo.setPartPaymentAllowed('N');
			billInfo.setOverrideAccHeadAllowed('N');
			billInfo.setDescription("Leases And Agreements : " + (StringUtils.isBlank(agreement.getAgreementNumber())
					? agreement.getAcknowledgementNumber() : agreement.getAgreementNumber()));
			LOGGER.info("after billInfo.setDescription>>>>>>>" + billInfo.getDescription());

			billInfo.setConsumerCode(StringUtils.isBlank(agreement.getAgreementNumber())
					? agreement.getAcknowledgementNumber() : agreement.getAgreementNumber());
			billInfo.setCallbackForApportion('N');
			LOGGER.info("after billInfo.setConsumerCode>>>>>>>" + billInfo.getConsumerCode());

			billInfo.setEmailId(agreement.getAllottee().getEmailId());
			billInfo.setConsumerType("Agreement");
			LOGGER.info("before Bill Number" + billNumberService.generateBillNumber());
			billInfo.setBillNumber(billNumberService.generateBillNumber());
			LOGGER.info("after Bill Number" + billNumberService.generateBillNumber());
			DemandSearchCriteria demandSearchCriteria = new DemandSearchCriteria();
			demandSearchCriteria.setDemandId(Long.valueOf(agreement.getDemands().get(0)));

			LOGGER.info("demand before>>>>>>>" + demandSearchCriteria);

			Demand demand = demandRepository.getDemandBySearch(demandSearchCriteria, requestInfo).getDemands().get(0);
			LOGGER.info("demand>>>>>>>" + demand);

			billInfo.setDisplayMessage(demand.getModuleName());
			billInfo.setMinAmountPayable(demand.getMinAmountPayable());

			lamsGetRequest.setName("FUNCTION_CODE");
			String functionCode = lamsConfigurationService.getLamsConfigurations(lamsGetRequest).get("FUNCTION_CODE")
					.get(0);
			BigDecimal totalAmount = BigDecimal.ZERO;
			List<BillDetailInfo> billDetailInfos = new ArrayList<>();
			int orderNo = 0;
			System.out.print("PaymentService- generateBillXml - getting purpose");
			Map<String, String> purposeMap = billRepository.getPurpose();
			for (DemandDetails demandDetail : demand.getDemandDetails()) {
				orderNo++;
				totalAmount = totalAmount.add(demandDetail.getTaxAmount().subtract(demandDetail.getCollectionAmount()));
				billDetailInfos.addAll(getBilldetails(demandDetail, functionCode, orderNo, requestInfo, purposeMap));
			}
			billInfo.setTotalAmount(totalAmount.doubleValue());
			billInfo.setBillAmount(totalAmount.doubleValue());
			billInfo.setBillDetailInfos(billDetailInfos);
			LOGGER.info("billInfo before>>>>>>>" + billInfo);
			billInfos.add(billInfo);
			final String billXml = billRepository.createBillAndGetXml(billInfos, requestInfo);

			try {
				collectXML = URLEncoder.encode(billXml, "UTF-8");
			} catch (final UnsupportedEncodingException e) {
				throw new RuntimeException(e.getMessage());
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return collectXML;
	}

	public List<BillDetailInfo> getBilldetails(final DemandDetails demandDetail, String functionCode, int orderNo,
			RequestInfo requestInfo, Map<String, String> purpose) {
		final List<BillDetailInfo> billDetails = new ArrayList<>();

		LOGGER.info("paymentservice demand detail ::"+demandDetail);
		try {
			BillDetailInfo billdetail = new BillDetailInfo();
			// TODO: Fix me: As per the rules for the order no.
			billdetail.setOrderNo(orderNo);
			billdetail.setCreditAmount(demandDetail.getTaxAmount().subtract(demandDetail.getCollectionAmount()));
			billdetail.setDebitAmount(BigDecimal.ZERO);
			LOGGER.info("getGlCode before>>>>>>>" + demandDetail.getGlCode());
			billdetail.setGlCode(getGlcodeById(demandDetail.getGlCode(), demandDetail.getTenantId(), requestInfo));
			LOGGER.info("getGlCode after >>>>>>>" + demandDetail.getGlCode());
			billdetail.setDescription(demandDetail.getTaxPeriod().concat(":").concat(demandDetail.getTaxReason()));
			billdetail.setPeriod(demandDetail.getTaxPeriod());
			// TODO: Fix me: As per the rules for the purpose for demanddetails
			billdetail.setPurpose(purpose.get("CURRENT_AMOUNT").toString());
			LOGGER.info("getPurpose after >>>>>>>" + purpose.get("CURRENT_AMOUNT"));

			billdetail.setIsActualDemand(demandDetail.getIsActualDemand());
			billdetail.setFunctionCode(functionCode);
			billDetails.add(billdetail);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return billDetails;
	}

	public ResponseEntity<ReceiptAmountInfo> updateDemand(BillReceiptInfoReq billReceiptInfoReq) {
		System.out.print("PaymentService- updateDemand - billReceiptInfoReq::: - "
				+ billReceiptInfoReq.getBillReceiptInfo().getBillReferenceNum());

		RequestInfo requestInfo = billReceiptInfoReq.getRequestInfo();
		BillSearchCriteria billSearchCriteria = new BillSearchCriteria();
		DemandSearchCriteria demandSearchCriteria = new DemandSearchCriteria();
		BillReceiptReq billReceiptInfo = billReceiptInfoReq.getBillReceiptInfo();
		billSearchCriteria.setBillId(Long.valueOf(billReceiptInfo.getBillReferenceNum()));
		BillInfo billInfo = billRepository.searchBill(billSearchCriteria, requestInfo);
		System.out.print("PaymentService- updateDemand - billInfo - " + billInfo.getBillNumber());
		demandSearchCriteria.setDemandId(billInfo.getDemandId());
		Demand currentDemand = demandRepository.getDemandBySearch(demandSearchCriteria, requestInfo).getDemands()
				.get(0);
		System.out.print("PaymentService- updateDemand - currentDemand - " + currentDemand.getId());
		if (currentDemand.getMinAmountPayable() != null && currentDemand.getMinAmountPayable() > 0)
			currentDemand.setMinAmountPayable(0d);

		updateDemandDetailForReceiptCreate(currentDemand, billReceiptInfoReq.getBillReceiptInfo());
		System.out.print("PaymentService- updateDemand - updateDemandDetailForReceiptCreate done");
		LOGGER.info("The amount collected from citizen is ::: " + currentDemand.getCollectionAmount());
		currentDemand.setPaymentInfos(setPaymentInfos(billReceiptInfo));
		demandRepository.updateDemand(Arrays.asList(currentDemand), requestInfo).getDemands().get(0);
		System.out.print("PaymentService- updateDemand - setPaymentInfos done");

		// / FIXME put update workflow here here
		updateWorkflow(billInfo.getConsumerCode(), requestInfo);
		LOGGER.info("the consumer code from bill object ::: " + billInfo.getConsumerCode());
		return receiptAmountBifurcation(billReceiptInfo, billInfo);
	}

	private void updateWorkflow(String consumerCode, RequestInfo requestInfo) {

		// FIXME get the query String from query builder //FIXME do the
		// jdbctemplate in repository
		String sql = "select *,agreement.id as agreementid from eglams_agreement agreement "
				+ "INNER JOIN eglams_demand demand ON agreement.id=demand.agreementid"
				+ " where agreement.acknowledgementnumber='" + consumerCode + "' OR agreement.agreement_no='"
				+ consumerCode + "'";

		LOGGER.info("the sql query for fetching agreement using consumercode ::: " + sql);
		List<Agreement> agreements = null;
		try {
			agreements = jdbcTemplate.query(sql, new AgreementRowMapper());
		} catch (DataAccessException e) {
			e.printStackTrace();
			LOGGER.info("exception while fetching agreemment in paymentService");
		}
		LOGGER.info("the result form jdbc query ::: " + agreements);
		AgreementRequest agreementRequest = new AgreementRequest();
		agreementRequest.setRequestInfo(requestInfo);
		agreementRequest.setAgreement(agreements.get(0));
		LOGGER.info("calling agreement service todo agreement update");
		agreementService.updateAgreement(agreementRequest);
		LOGGER.info("Workflow update for collection has been put into Kafka Queue");
	}

	private List<PaymentInfo> setPaymentInfos(BillReceiptReq billReceiptInfo) {

		List<PaymentInfo> paymentInfos = new ArrayList<>();
		PaymentInfo paymentInfo = new PaymentInfo();
		paymentInfo.setReceiptAmount(billReceiptInfo.getTotalAmount().toString());
		paymentInfo.setReceiptDate(billReceiptInfo.getReceiptDate().toString());
		paymentInfo.setReceiptNumber(billReceiptInfo.getReceiptNum());
		paymentInfo.setStatus(billReceiptInfo.getReceiptStatus());
		return paymentInfos;

	}

	private void updateDemandDetailForReceiptCreate(Demand demand, BillReceiptReq billReceiptInfo) {
		BigDecimal totalAmountCollected = BigDecimal.ZERO;
		LOGGER.info("the size of objects ::: " + billReceiptInfo.getAccountDetails().size()
				+ "the size of demand details ::" + demand.getDemandDetails().size());
		for (final ReceiptAccountInfo rcptAccInfo : billReceiptInfo.getAccountDetails()) {

			totalAmountCollected = totalAmountCollected.add(updateDemandDetails(demand, rcptAccInfo));
		}
		LOGGER.info("updateDemandDetailForReceiptCreate  ::: totalAmountCollected " + totalAmountCollected);
		demand.setCollectionAmount(totalAmountCollected);
	}

	private BigDecimal updateDemandDetails(Demand demand, final ReceiptAccountInfo rcptAccInfo) {

		BigDecimal totalAmountCollected = BigDecimal.ZERO;

		LOGGER.info("updateDemandDetailForReceiptCreate rcptAccInfo ::: " + rcptAccInfo);
		if (rcptAccInfo.getCrAmount() != null && rcptAccInfo.getCrAmount() > 0 && !rcptAccInfo.isRevenueAccount()

				&& rcptAccInfo.getDescription() != null) {
			String[] description = rcptAccInfo.getDescription().split(":");
			String taxPeriod = description[0];
			String taxReason = description[1];
			LOGGER.info("taxPeriod  ::: " + taxPeriod + "taxReason ::::::" + taxReason);
			// updating the existing demand detail..
			for (final DemandDetails demandDetail : demand.getDemandDetails()) {
				LOGGER.info("demandDetail.getTaxPeriod()  ::: " + demandDetail.getTaxPeriod()
						+ "demandDetail.getTaxReason() ::::::" + demandDetail.getTaxReason());
				if (demandDetail.getTaxPeriod() != null && demandDetail.getTaxPeriod().equalsIgnoreCase(taxPeriod)
						&& demandDetail.getTaxReason() != null
						&& demandDetail.getTaxReason().equalsIgnoreCase(taxReason)) {
					demandDetail.setCollectionAmount(BigDecimal.valueOf(rcptAccInfo.getCrAmount()));
					totalAmountCollected = totalAmountCollected.add(BigDecimal.valueOf(rcptAccInfo.getCrAmount()));
					LOGGER.info("everytime totalAmountCollected ::: " + totalAmountCollected);
				}
			}
		}

		return totalAmountCollected;
	}

	public ResponseEntity<ReceiptAmountInfo> receiptAmountBifurcation(final BillReceiptReq billReceiptInfo,
			BillInfo billInfo) {
		ResponseEntity<ReceiptAmountInfo> receiptAmountInfoResponse = null;
		System.out.print("PaymentService- receiptAmountBifurcation - billReceiptInfo - " + billReceiptInfo);
		System.out.print("PaymentService- receiptAmountBifurcation - billInfo - " + billInfo);
		final ReceiptAmountInfo receiptAmountInfo = new ReceiptAmountInfo();
		BigDecimal currentInstallmentAmount = BigDecimal.ZERO;
		BigDecimal arrearAmount = BigDecimal.ZERO;
		System.out.print("PaymentService- receiptAmountBifurcation - getting purpose");
		Map<String, String> purposeMap = billRepository.getPurpose();
		final List<BillDetailInfo> billDetails = new ArrayList<>(billInfo.getBillDetailInfos());
		for (final ReceiptAccountInfo rcptAccInfo : billReceiptInfo.getAccountDetails()) {
			System.out.print("PaymentService- receiptAmountBifurcation - rcptAccInfo - " + rcptAccInfo);
			if (rcptAccInfo.getCrAmount() != null
					&& BigDecimal.valueOf(rcptAccInfo.getCrAmount()).compareTo(BigDecimal.ZERO) == 1) {
				if (rcptAccInfo.getPurpose().equals(purposeMap.get("ARREAR_AMOUNT").toString()))
					arrearAmount = arrearAmount.add(BigDecimal.valueOf(rcptAccInfo.getCrAmount()));
				else
					currentInstallmentAmount = currentInstallmentAmount
							.add(BigDecimal.valueOf(rcptAccInfo.getCrAmount()));

				for (final BillDetailInfo billDet : billDetails) {
					if (billDet.getOrderNo() == 1) {
						receiptAmountInfo.setInstallmentFrom(billDet.getDescription());
					}
					receiptAmountInfo.setCurrentInstallmentAmount(currentInstallmentAmount.doubleValue());
					receiptAmountInfo.setArrearsAmount(arrearAmount.doubleValue());
					receiptAmountInfoResponse = new ResponseEntity<>(receiptAmountInfo, HttpStatus.OK);
				}
			}
			System.out.print("PaymentService- receiptAmountBifurcation - receiptAmountInfo - " + receiptAmountInfo);
		}
		return receiptAmountInfoResponse;
	}

	private String getGlcodeById(Long id, String tenantId, RequestInfo requestInfo) {
		ChartOfAccountContract chartOfAccountContract = new ChartOfAccountContract();
		chartOfAccountContract.setId(id);
		chartOfAccountContract.setTenantId(tenantId);
		return financialsRepository.getChartOfAccountGlcodeById(chartOfAccountContract, requestInfo);
	}

	private BoundaryResponse getBoundariesById(Long boundaryId,String tenantId) {

		BoundaryResponse boundaryResponse = null;
		String boundaryUrl = propertiesManager.getBoundaryServiceHostName()
							+ propertiesManager.getBoundaryServiceSearchPath() 
							+ "?Boundary.id=" + boundaryId
							+ "&Boundary.tenantId=" + tenantId;
		// FIXME in boundary contract id is string
		LOGGER.info("the boundary url from payment service ::"+boundaryUrl);
		try {
			boundaryResponse = restTemplate.getForObject(boundaryUrl, BoundaryResponse.class);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.info("the exception thrown from boundary request is :: " + e);
		}
		LOGGER.info("the response from boundary ::"+boundaryResponse);
		return boundaryResponse;
	}

}
