package ca.uhn.cip.modeltracker.service;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import ca.uhn.cip.modeltracker.controller.BioSampleController;
import ca.uhn.cip.modeltracker.model.BioSample;
import ca.uhn.cip.modeltracker.model.DataCount;
import ca.uhn.cip.modeltracker.model.DataFieldOption;
import ca.uhn.cip.modeltracker.model.Model;
import ca.uhn.cip.modeltracker.model.STRAnalysis;
import ca.uhn.cip.modeltracker.model.STRAnalysisId;
import ca.uhn.cip.modeltracker.model.Study;
import ca.uhn.cip.modeltracker.model.STRData;
import ca.uhn.cip.modeltracker.model.STRError;
import ca.uhn.cip.modeltracker.model.STRMatch;
import ca.uhn.cip.modeltracker.repository.BioSampleRepository;
import ca.uhn.cip.modeltracker.repository.DataFieldOptionRepository;
import ca.uhn.cip.modeltracker.repository.ModelRepository;
import ca.uhn.cip.modeltracker.repository.STRAnalysisRepository;
import ca.uhn.cip.modeltracker.repository.StudyRepository;
import ca.uhn.cip.modeltracker.util.DuplicateEntityFoundException;
import ca.uhn.cip.modeltracker.util.EntityNotFoundException;
import ca.uhn.cip.modeltracker.repository.STRDataRepository;

/**
 * Spring boot service class for biosample.
 * 
 * @author Kevin Wang
 *
 */

@Service
public class BioSampleServiceImpl implements BioSampleService {

    public static final Logger logger = LoggerFactory.getLogger(BioSampleServiceImpl.class);

	@Autowired
	BioSampleRepository biosampleRepository;

	@Autowired
	ModelRepository modelRepository;

	@Autowired
	StudyRepository studyRepository;

	@Autowired
	STRAnalysisRepository strAnalysisRepository;

	@Autowired
	STRDataRepository strDataRepository;
	
	@Autowired
	DataFieldOptionRepository dataFieldOptionRepository;

	@Override
	public BioSample saveBioSample(BioSample sample) {
		return biosampleRepository.save(sample);
	}
	
	@Override
	public List<BioSample> saveBioSampleList(List<BioSample> sampleList, List<STRError> strErrorList) {
		
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("uuuu-MM-dd", Locale.US).withResolverStyle(ResolverStyle.STRICT);;
		
		List<BioSample> retSampleList = new ArrayList<BioSample>();
		
		// business logic is not fixed so far. 
		// we need to sort the datalist first using a map with key of sample descriptor and receiving date. value is the biomarker count
		for (BioSample sample: sampleList) {
			// go through the list to add each STR analysis data
			
			// remove the extra white space before or after each field
			BioSample data = removeSampleDataWhiteSpace(sample);
			
			// Check if the mandatory fields are empty. if so skip this item
			if (data.getSampleDescriptor().isEmpty()) {
				STRError strError = new STRError();
				strError.setErrorCode(HttpStatus.CONFLICT.value());
				strError.setErrorSTRSampleID(data.getSampleDescriptor());
				strError.setErrorText(" sample descriptor cannot be empty. ");
				strErrorList.add(strError);
				continue;
			}
			
			// check if sample descriptor is duplicate
			List<BioSample> foundSampleList = this.findBioSamplesBySampleIdIgnoreCase(data.getSampleDescriptor());
			if (foundSampleList != null && foundSampleList.size()>0) {
				STRError strError = new STRError();
				strError.setErrorCode(HttpStatus.CONFLICT.value());
				strError.setErrorSTRSampleID(data.getSampleDescriptor());
				strError.setErrorText(" duplicate sample descriptor found in the database. ");
				strErrorList.add(strError);
				continue;
			}				
			
			// check if model display name is from the existing list
			if (!data.getModelDisplayName().isEmpty()) {
				Model retModel = this.findModelByModelDisplayName(data.getModelDisplayName());
				if (retModel == null) {
					STRError strError = new STRError();
					strError.setErrorCode(HttpStatus.CONFLICT.value());
					strError.setErrorSTRSampleID(data.getSampleDescriptor());
					strError.setErrorText(" invalid model display name used for biosample. ");
					strErrorList.add(strError);
					continue;
				}
				
				// use the PPID defined for model and discard the user entered PPID in csv to ensure consistency
				data.setProtocolParticipantID(retModel.getProtocolParticipantID());
			} else if (data.getModelDisplayName().isEmpty()) { 
				// check if ppid is empty when model display name is empty already
				// this only applies when user does not specify model and only specify the ppid (sample is not model derived/related to a model)
				if (data.getProtocolParticipantID().isEmpty()) {
					STRError strError = new STRError();
					strError.setErrorCode(HttpStatus.CONFLICT.value());
					strError.setErrorSTRSampleID(data.getSampleDescriptor());
					strError.setErrorText(" PPID cannot be empty. ");
					strErrorList.add(strError);
					continue;
				}
			}
			
			// check if sample type is empty
			if (data.getSampleType().isEmpty()) {
				STRError strError = new STRError();
				strError.setErrorCode(HttpStatus.CONFLICT.value());
				strError.setErrorSTRSampleID(data.getSampleDescriptor());
				strError.setErrorText(" sample type cannot be empty. ");
				strErrorList.add(strError);
				continue;
			}

			// check if sample type is not from the type list
			List<DataFieldOption> sampleTypeOptions = this.findDataFieldOptionsByFieldOptionName("sampleType");
			boolean foundOption = false;
			for (DataFieldOption item: sampleTypeOptions) {
				if (data.getSampleType().equalsIgnoreCase(item.getFieldOptionValue())) {
					foundOption = true;
					break;
				}
			}
			if (!foundOption) {
				STRError strError = new STRError();
				strError.setErrorCode(HttpStatus.CONFLICT.value());
				strError.setErrorSTRSampleID(data.getSampleDescriptor());
				strError.setErrorText(" sample type is invalid. ");
				strErrorList.add(strError);
				continue;
			}

			// check if disease site is empty
			if (data.getPrimaryDiseaseSite().isEmpty()) {
				STRError strError = new STRError();
				strError.setErrorCode(HttpStatus.CONFLICT.value());
				strError.setErrorSTRSampleID(data.getSampleDescriptor());
				strError.setErrorText(" primary disease site cannot be empty. ");
				strErrorList.add(strError);
				continue;
			}
			
			// check if disease site is from the site list
			List<DataFieldOption> diseaseSiteOption = this.findDataFieldOptionsByFieldOptionName("primaryDiseaseSite");
			boolean foundDiseaseSiteOption = false;
			for (DataFieldOption item: diseaseSiteOption) {
				if (data.getPrimaryDiseaseSite().equalsIgnoreCase(item.getFieldOptionValue())) {
					foundDiseaseSiteOption = true;
					break;
				}
			}
			if (!foundDiseaseSiteOption) {
				STRError strError = new STRError();
				strError.setErrorCode(HttpStatus.CONFLICT.value());
				strError.setErrorSTRSampleID(data.getSampleDescriptor());
				strError.setErrorText(" primary disease site is invalid. ");
				strErrorList.add(strError);
				continue;
			}
			
			// check if intervention start date is valid date
			if (!data.getInterventionStartDate().isEmpty()) {
				try {
					dateFormatter.parse(data.getInterventionStartDate());
				} catch (DateTimeParseException e) {
					STRError strError = new STRError();
					strError.setErrorCode(HttpStatus.CONFLICT.value());
					strError.setErrorSTRSampleID(data.getSampleDescriptor());
					strError.setErrorText(" intervention start date is invalid. ");
					strErrorList.add(strError);
					continue;
				}
			}
			
			// check if intervention end date is valid date
			if (!data.getInterventionEndDate().isEmpty()) {
				try {
					dateFormatter.parse(data.getInterventionEndDate());
				} catch (DateTimeParseException e) {
					STRError strError = new STRError();
					strError.setErrorCode(HttpStatus.CONFLICT.value());
					strError.setErrorSTRSampleID(data.getSampleDescriptor());
					strError.setErrorText(" intervention end date is invalid. ");
					strErrorList.add(strError);
					continue;
				}
			}

			BioSample retData = this.saveBioSample(data);
			if (retData != null) {
				retSampleList.add(retData);
			}
		}
		
		return retSampleList;
	}


	@Override
	public List<BioSample> findAllBioSamples() {
        List<BioSample> biosampleList = new ArrayList<BioSample>();
        // List<BioSample> biosampleIter = biosampleRepository.findAll();
        biosampleRepository.findAll().forEach(biosample -> biosampleList.add(biosample));
        return biosampleList;
	}
	
	@Override
	public Page<BioSample> findAllBioSamplesPageable(Pageable pageable) {
		return biosampleRepository.findAll(pageable);
	}

	@Override
	public List<BioSample> findBioSamplesBySampleIdIgnoreCase(String sampleId) {
        List<BioSample> biosampleList = new ArrayList<BioSample>();
        biosampleRepository.findBySampleDescriptorIgnoreCase(sampleId).forEach(biosample -> biosampleList.add(biosample));
        return biosampleList;
	}

	@Override
	public List<BioSample> findBioSamplesBySampleIdIgnoreCaseContaining(String sampleId) {
        List<BioSample> biosampleList = new ArrayList<BioSample>();
        biosampleRepository.findBySampleDescriptorIgnoreCaseContaining(sampleId).forEach(biosample -> biosampleList.add(biosample));
        return biosampleList;
	}

	@Override
	public Page<BioSample> findBioSamplesPageableBySampleIdIgnoreCase(String sampleId, Pageable pageable) {
		return biosampleRepository.findBySampleDescriptorIgnoreCase(sampleId, pageable);
	}

	@Override
	public Page<BioSample> findBioSamplesPageableBySampleIdIgnoreCaseContaining(String sampleId, Pageable pageable) {
		return biosampleRepository.findBySampleDescriptorIgnoreCaseContaining(sampleId, pageable);
	}

	@Override
	public List<BioSample> findBioSamplesByModelIdIgnoreCase(String modelId) {
        List<BioSample> biosampleList = new ArrayList<BioSample>();
        biosampleRepository.findByModelDisplayNameIgnoreCase(modelId).forEach(biosample -> biosampleList.add(biosample));
        return biosampleList;
	}

	@Override
	public List<BioSample> findBioSamplesByModelIdIgnoreCaseContaining(String modelId) {
        List<BioSample> biosampleList = new ArrayList<BioSample>();
        biosampleRepository.findByModelDisplayNameIgnoreCaseContaining(modelId).forEach(biosample -> biosampleList.add(biosample));
        return biosampleList;
	}
	
	@Override
	public Page<BioSample> findBioSamplesPageableByModelIdIgnoreCase(String modelId, Pageable pageable) {
		return biosampleRepository.findByModelDisplayNameIgnoreCase(modelId, pageable);
	}

	@Override
	public Page<BioSample> findBioSamplesPageableByModelIdIgnoreCaseContaining(String modelId, Pageable pageable) {
		return biosampleRepository.findByModelDisplayNameIgnoreCaseContaining(modelId, pageable);
	}

	@Override
	public List<BioSample> findBioSamplesByPPIdIgnoreCase(String ppid) {
        List<BioSample> biosampleList = new ArrayList<BioSample>();
        biosampleRepository.findByProtocolParticipantID(ppid).forEach(biosample -> biosampleList.add(biosample));
        return biosampleList;
	}

	@Override
	public List<BioSample> findBioSamplesByPPIdIgnoreCaseContaining(String ppid) {
        List<BioSample> biosampleList = new ArrayList<BioSample>();
        biosampleRepository.findByProtocolParticipantIDIgnoreCaseContaining(ppid).forEach(biosample -> biosampleList.add(biosample));
        return biosampleList;
	}

	@Override
	public List<BioSample> findBioSamplesByPPIdList(List<String> ppids) {
        List<BioSample> biosampleList = biosampleRepository.findByProtocolParticipantIDIn(ppids);
		if (biosampleList != null && ! biosampleList.isEmpty()) {
			return biosampleList;
		} else {
			return null;
		}
	}

	@Override
	public List<BioSample> findBioSamplesByPPIdListContaining(List<String> ppids) {
        List<BioSample> biosampleList = biosampleRepository.findByProtocolParticipantIDIn(ppids);
		if (biosampleList != null && ! biosampleList.isEmpty()) {
			return biosampleList;
		} else {
			return null;
		}
	}

	@Override
	public Page<BioSample> findBioSamplesPageableByPPIdIgnoreCaseContaining(String ppid, Pageable pageable) {
		return biosampleRepository.findByProtocolParticipantIDIgnoreCaseContaining(ppid, pageable);
	}
	
	@Override
	public Page<BioSample> findBioSamplesPageableByPPIdList(List<String> ppids, Pageable pageable) {
        Page<BioSample> biosampleList = biosampleRepository.findByProtocolParticipantIDIn(ppids, pageable);
		if (biosampleList != null && ! biosampleList.isEmpty()) {
			return biosampleList;
		} else {
			return null;
		}
	}
	
	@Override
	public Page<BioSample> findBioSamplesPageableByPPIdListContaining(List<String> ppids, Pageable pageable) {
        Page<BioSample> biosampleList = biosampleRepository.findByProtocolParticipantIDIn(ppids, pageable);
		if (biosampleList != null && ! biosampleList.isEmpty()) {
			return biosampleList;
		} else {
			return null;
		}
	}

 	@Override
	public BioSample findBioSampleById(Long id) {
		return biosampleRepository.findById(id).isPresent() ? biosampleRepository.findById(id).get(): null;
	}

	@Override
	public BioSample updateBioSampleById(Long id, BioSample biosample) {
		BioSample retBiosample = biosampleRepository.findById(id).isPresent() ? biosampleRepository.findById(id).get(): null;
		if (retBiosample != null) {
			// retBiosample.setSampleDescriptor(biosample.getSampleDescriptor());
			retBiosample.setModelDisplayName(biosample.getModelDisplayName());
			retBiosample.setProtocolParticipantID(biosample.getProtocolParticipantID());
			retBiosample.setBarcode(biosample.getBarcode());
			// retBiosample.setCollectionDate(biosample.getCollectionDate());
			retBiosample.setSampleType(biosample.getSampleType());
			retBiosample.setPrimaryDiseaseSite(biosample.getPrimaryDiseaseSite());
			retBiosample.setInterventionDescription(biosample.getInterventionDescription());
			retBiosample.setInterventionStartDate(biosample.getInterventionStartDate());
			retBiosample.setInterventionEndDate(biosample.getInterventionEndDate());
			retBiosample.setComment(biosample.getComment());
		}
		retBiosample = biosampleRepository.save(retBiosample);
		return retBiosample;
	}

	@Override
	public BioSample deleteBioSampleById(Long id) {
		BioSample biosample = biosampleRepository.findById(id).isPresent() ? biosampleRepository.findById(id).get(): null;
		if (biosample != null) {
			biosampleRepository.deleteById(id);
		}
		return biosample;
	}
	
	@Override
	public long getNumberOfBioSamples() {
		return biosampleRepository.count();
	}

	@Override
	public List<DataCount> getBioSampleAnalyticsByField(String groupField){
		List<DataCount> retList = new ArrayList<DataCount>();
		List<Object[]> countList = null;
		
		if (groupField.equalsIgnoreCase("primary_disease_site")) {
			countList = biosampleRepository.getCountByDiseaseSite();
		} else if (groupField.equalsIgnoreCase("sample_type")) {
			countList = biosampleRepository.getCountBySampleType();
		} else {
			countList = null;
		}
		
		if(countList != null && !countList.isEmpty()){
			for (Object[] object : countList) {
				DataCount count = new DataCount();
				count.setDataType((String)object[1]);
				count.setCount((Long)object[0]);
				retList.add(count);
			}
		}
		
		return retList;
	}


	@Override
	public STRAnalysis saveSTRAnalysis(STRAnalysis strAnalysis) {
		return strAnalysisRepository.save(strAnalysis);
	}
	
	@Override
	public List<STRAnalysis> findAllSTRAnalyses() {
        List<STRAnalysis> strAnalysisList = new ArrayList<STRAnalysis>();
        strAnalysisRepository.findAll().forEach(strAnalysis -> strAnalysisList.add(strAnalysis));
        return strAnalysisList;		
	}

	@Override
	public Page<STRAnalysis> findAllSTRAnalysesPageable(Pageable pageable) {
		return strAnalysisRepository.findAll(pageable);
	}

	@Override
	public List<STRAnalysis> findAllReferenceSTRAnalyses() {
        List<STRAnalysis> strAnalysisList = new ArrayList<STRAnalysis>();
        strAnalysisRepository.findByIsReferenceStrTrue().forEach(strAnalysis -> strAnalysisList.add(strAnalysis));
        return strAnalysisList;		
	}

	@Override
	public List<STRAnalysis> findSTRAnalysesBySampleIdIgnoreCase(String sampleId) {
        List<STRAnalysis> strAnalysisList = new ArrayList<STRAnalysis>();
        strAnalysisRepository.findBySampleIdIgnoreCase(sampleId).forEach(strAnalysis -> strAnalysisList.add(strAnalysis));
        return strAnalysisList;		
	}

	@Override
	public List<STRAnalysis> findSTRAnalysesBySampleIdIgnoreCaseContaining(String sampleId) {
        List<STRAnalysis> strAnalysisList = new ArrayList<STRAnalysis>();
        sampleId = sampleId.replace("_", "@_");
        sampleId = "%" + sampleId + "%";
        strAnalysisRepository.findBySampleIdIgnoreCaseContaining(sampleId).forEach(strAnalysis -> strAnalysisList.add(strAnalysis));
        return strAnalysisList;		
	}

	@Override
	public Page<STRAnalysis> findSTRAnalysesPageableBySampleIdIgnoreCase(String sampleId, Pageable pageable) {
		return strAnalysisRepository.findBySampleIdIgnoreCase(sampleId, pageable);
	}

	@Override
	public Page<STRAnalysis> findSTRAnalysesPageableBySampleIdIgnoreCaseContaining(String sampleId, Pageable pageable) {
        sampleId = sampleId.replace("_", "@_");
        sampleId = "%" + sampleId + "%";
		return strAnalysisRepository.findBySampleIdIgnoreCaseContaining(sampleId, pageable);
	}

	@Override
	public List<STRAnalysis> findSTRAnalysesByModelIdIgnoreCaseContaining(String modelId) {
        List<STRAnalysis> strAnalysisList = new ArrayList<STRAnalysis>();
        modelId = modelId.replace("_", "@_");
        modelId = "%" + modelId + "%";
        strAnalysisRepository.findByModelIdIgnoreCaseContaining(modelId).forEach(strAnalysis -> strAnalysisList.add(strAnalysis));
        return strAnalysisList;		
	}

	@Override
	public Page<STRAnalysis> findSTRAnalysesPageableByModelIdIgnoreCase(String modelId, Pageable pageable) {
		return strAnalysisRepository.findByModelIdIgnoreCase(modelId, pageable);
	}

	@Override
	public Page<STRAnalysis> findSTRAnalysesPageableByModelIdIgnoreCaseContaining(String modelId, Pageable pageable) {
        modelId = modelId.replace("_", "@_");
        modelId = "%" + modelId + "%";
		return strAnalysisRepository.findByModelIdIgnoreCaseContaining(modelId, pageable);
	}

	@Override
	public List<STRAnalysis> findSTRAnalysesByModelIdIgnoreCase(String modelId) {
        List<STRAnalysis> strAnalysisList = new ArrayList<STRAnalysis>();
        strAnalysisRepository.findByModelId(modelId).forEach(strAnalysis -> strAnalysisList.add(strAnalysis));
        return strAnalysisList;		
	}

	@Override
	public List<STRAnalysis> findSTRAnalysesByPPIdIgnoreCase(String ppid) {
        List<STRAnalysis> strAnalysisList = new ArrayList<STRAnalysis>();
        strAnalysisRepository.findByPPIdIgnoreCase(ppid).forEach(strAnalysis -> strAnalysisList.add(strAnalysis));
        return strAnalysisList;				
	}

	@Override
	public List<STRAnalysis> findSTRAnalysesByPPIdIgnoreCaseContaining(String ppid) {
        List<STRAnalysis> strAnalysisList = new ArrayList<STRAnalysis>();
        ppid = ppid.replace("_", "@_");
        ppid = "%" + ppid + "%";
        strAnalysisRepository.findByPPIdIgnoreCaseContaining(ppid).forEach(strAnalysis -> strAnalysisList.add(strAnalysis));
        return strAnalysisList;				
	}

	@Override
	public List<STRAnalysis> findSTRAnalysesByPPIdList(List<String> ppids) {
		return strAnalysisRepository.findByPPIdIn(ppids);
	}

	@Override
	public List<STRAnalysis> findSTRAnalysesByPPIdListContaining(List<String> ppids) {
		return strAnalysisRepository.findByPPIdIn(ppids);
	}
	
	@Override
	public Page<STRAnalysis> findSTRAnalysesPageableByPPIdIgnoreCaseContaining(String ppid, Pageable pageable) {
        ppid = ppid.replace("_", "@_");
        ppid = "%" + ppid + "%";
        return strAnalysisRepository.findByPPIdIgnoreCaseContaining(ppid, pageable);
	}

	@Override
	public Page<STRAnalysis> findSTRAnalysesPageableByPPIdList(List<String> ppids, Pageable pageable) {
		return strAnalysisRepository.findByPPIdIn(ppids, pageable);
	}
	
	@Override
	public Page<STRAnalysis> findSTRAnalysesPageableByPPIdListContaining(List<String> ppids, Pageable pageable) {
		return strAnalysisRepository.findByPPIdIn(ppids, pageable);
	}

	@Override
	public List<STRAnalysis> findSTRAnalysesBySampleDescriptor(String sampleDescriptor) {
        List<STRAnalysis> strAnalysisList = new ArrayList<STRAnalysis>();
        strAnalysisRepository.findBySampleDescriptorIgnoreCase(sampleDescriptor).forEach(strAnalysis -> strAnalysisList.add(strAnalysis));
        return strAnalysisList;
	}
	
	@Override
	public List<STRAnalysis> findSTRAnalysesBySTRDataList(List<STRData> strDataList) {
		List<STRAnalysis> strAnalysisList =  null;
		HashMap<String, STRAnalysis> strAnalysisMap = new HashMap<String, STRAnalysis>();
		
		for (int i = 0; i < strDataList.size(); i++) {
			
			STRAnalysis strAnalysis = this.findSTRAnalysisBySampleDescriptorAndReceivingDate(strDataList.get(i).getSampleDescriptor(), strDataList.get(i).getReceivingDate());
			if (strAnalysis != null) {
				if (strAnalysisMap.containsKey(strAnalysis.getSampleDescriptor()+strAnalysis.getReceivingDate())) {
					
				} else {
					strAnalysisMap.put(strAnalysis.getSampleDescriptor()+strAnalysis.getReceivingDate(), strAnalysis);
				}
			}
		}
		
		strAnalysisList = new ArrayList<STRAnalysis>(strAnalysisMap.values());
		return strAnalysisList;
	}
	
	@Override
	public STRAnalysis findRefSTRAnalysisBySTRAnalysis(STRAnalysis strAnalysis) {
	
		// here is the new logic. first check if the current strAnalysis has reference data, if so, return it
		if (strAnalysis.getRefSampleDescriptor() != null && strAnalysis.getRefReceivingDate() != null) {
			return this.findSTRAnalysisBySampleDescriptorAndReceivingDate(strAnalysis.getRefSampleDescriptor(), strAnalysis.getRefReceivingDate());
		}
		
		// next, check all strAnalysis for same ppid and return the reference strAnalysis if it is available
		List<STRAnalysis> strAnalysisList = this.findSTRAnalysesByPPIdIgnoreCase(strAnalysis.getSample().getProtocolParticipantID());	
		for (int i=0; i<strAnalysisList.size(); i++) {
			if (strAnalysisList.get(i).getRefSampleDescriptor() != null && strAnalysisList.get(i).getRefReceivingDate() != null) {
				return this.findSTRAnalysisBySampleDescriptorAndReceivingDate(strAnalysisList.get(i).getRefSampleDescriptor(), strAnalysisList.get(i).getRefReceivingDate());
			}
		}

		// last, if nothing available, return null
		return null;
	}

	@Override
	public STRAnalysis findSTRAnalysisBySampleDescriptorAndReceivingDate(String sampleDescriptor, String receivingDate) {
		Optional<STRAnalysis> analysis = strAnalysisRepository.findById(new STRAnalysisId(sampleDescriptor, receivingDate));
		if (analysis.isPresent()) {
			return analysis.get();
		} else {
			return null;
		}
	}

	@Override
	public STRAnalysis updateSTRAnalysisBySampleDescriptorAndReceivingDate(
			String sampleDescriptor, String receivingDate, STRAnalysis strAnalysis) {
		STRAnalysis retSTRAnalysis = null;
		
		Optional<STRAnalysis> analysis = strAnalysisRepository.findById(new STRAnalysisId(sampleDescriptor, receivingDate));
		if (analysis.isPresent()) {
			retSTRAnalysis = analysis.get();
			// retSTRAnalysis.setSampleDescriptor(strAnalysis.getSampleDescriptor());
			// retSTRAnalysis.setReceivingDate(strAnalysis.getReceivingDate());
			retSTRAnalysis.setIsReferenceStr(strAnalysis.getIsReferenceStr());
			retSTRAnalysis.setRefSampleDescriptor(strAnalysis.getRefSampleDescriptor());
			retSTRAnalysis.setRefReceivingDate(strAnalysis.getRefReceivingDate());
			retSTRAnalysis.setMatchCount(strAnalysis.getMatchCount());
			retSTRAnalysis.setTotalCount(strAnalysis.getTotalCount());
			retSTRAnalysis.setPercentageStrMatch(strAnalysis.getPercentageStrMatch());
			retSTRAnalysis.setComment(strAnalysis.getComment());
			// retSTRAnalysis.setSample(strAnalysis.getSample());
		}
		
		return retSTRAnalysis == null ? null : strAnalysisRepository.save(retSTRAnalysis);
	}

	@Override
	public STRAnalysis deleteSTRAnalysisBySampleDescriptorAndReceivingDate(String sampleDescriptor, String receivingDate) {
		
		// 1. delete the STR analysis
		Optional<STRAnalysis> analysis = strAnalysisRepository.findById(new STRAnalysisId(sampleDescriptor, receivingDate));
		if (analysis.isPresent()) {
			strAnalysisRepository.deleteById(new STRAnalysisId(sampleDescriptor, receivingDate));
		} else {
			return null;
		}
		
		// 2. also delete the STR data
		strDataRepository.deleteBySampleDescriptorAndReceivingDate(sampleDescriptor, receivingDate);
		
		return analysis.get();
	}

	@Override
	public long getNumberOfSTRAnalyses() {
		return strAnalysisRepository.count();
	}

	@Override
	public STRMatch computeSTRMatch(List<STRData> strDataRef, List<STRData> strDataSrc) {
		STRMatch retMatch = null;
		
		// 1. check which set has least number of biomarkers
		// this requires that all biomarkers analysis has overlapping biomarkers.
		// i.e. 24 counts analysis has all 16 counts and 16 counts has 10 counts.
		// if not satisfied, will need to check the overlapping set first
		if (strDataSrc.size() <= strDataRef.size()) {
			retMatch = this.computerSTRMatchBetweenTestAndBaseline(strDataRef, strDataSrc);
		} else {
			retMatch = this.computerSTRMatchBetweenTestAndBaseline(strDataSrc, strDataRef);
			
		}
		
		return retMatch;
	}
	
	private STRMatch computerSTRMatchBetweenTestAndBaseline(List<STRData> strDataTest, List<STRData> strDataBaseline) {
		int count = 0;
		int match = 0;
		boolean reviewMatch = false;
		
		// iterate through all the biomarkers in the baseline dataset
		for(int i=0; i < strDataBaseline.size(); i++) {
			STRData dataBaseline = strDataBaseline.get(i);
			String biomarkerNameBaseline = dataBaseline.getMarker();
			
			int biomarkerIndexTest = -1;
			
			// make sure the same biomarker exists in the test dataset
			// We won't assume that the biomarkers are ordered the same in both the test and reference STR results.
			for(int j=0; j < strDataTest.size(); j++) {
				STRData dataTest = strDataTest.get(j);
				String biomarkerNameTest = dataTest.getMarker();
				if (biomarkerNameTest.equalsIgnoreCase(biomarkerNameBaseline)) {
					biomarkerIndexTest = j;
					break;
				}
			}
			
			// Skip to the next biomarker if the current biomarker is not found in the reference STR results.
			if (biomarkerIndexTest < 0) continue;
			
			// if the counts are not 2 for both sets then set the warning to true so user need to review the result
			STRData dataTest = strDataTest.get(biomarkerIndexTest);
			List<Integer> alleleListBaseline = getValidAlleleList(dataBaseline);
			List<Integer> alleleListTest = getValidAlleleList(dataTest);
			int alleleCountBaseline = alleleListBaseline.size();
			int alleleCountTest = alleleListTest.size();
			if (alleleCountBaseline != 2 || alleleCountTest != 2) reviewMatch = true;
			
			// if the baseline data contains less alleles than test data
			if (alleleCountBaseline < alleleCountTest) {
				count = count + alleleCountBaseline;
				
				List<Boolean> alleleUsedForMatch = new ArrayList<Boolean>();
				for(int k = 0; k < alleleCountTest; k++) {
					alleleUsedForMatch.add(false);
				}
				
				for (int k = 0; k < alleleCountBaseline; k++) {
					for (int l = 0; l < alleleCountTest; l++) {
						if (alleleListBaseline.get(k) == alleleListTest.get(l)) {
							if (!alleleUsedForMatch.get(l)) {
								match++;
								alleleUsedForMatch.set(l, true);
								break;
							}
						}
					}
				}
			} else {
				count = count + alleleCountTest;
				
				List<Boolean> alleleUsedForMatch = new ArrayList<Boolean>();
				for(int k = 0; k < alleleCountBaseline; k++) {
					alleleUsedForMatch.add(false);
				}
				
				for (int k = 0; k < alleleCountTest; k++) {
					for (int l = 0; l < alleleCountBaseline; l++) {
						if (alleleListTest.get(k) == alleleListBaseline.get(l)) {
							if (!alleleUsedForMatch.get(l)) {
								match++;
								alleleUsedForMatch.set(l, true);
								break;
							}
						}
					}
				}
			}
		}
		
		float percentageMatch = 0.0f;
		if (count != 0) {
			percentageMatch = (float)match/(float)count;
		}
		
		STRMatch retMatch = new STRMatch();
		retMatch.setMatchCount(match);
		retMatch.setTotalCount(count);
		retMatch.setPercentageStrMatch(percentageMatch);
		return retMatch;
	}
	
	private static int getValidAlleleCount(STRData strData) {
		int count = 0;
		
		if (!strData.getAllele1().isEmpty()) count++;
		if (!strData.getAllele2().isEmpty()) count++;
		if (!strData.getAllele3().isEmpty()) count++;
		if (!strData.getAllele4().isEmpty()) count++;

		return count;
	}
	
	private static List<Integer> getValidAlleleList(STRData strData) {
		List<Integer> alleleArray = new ArrayList<Integer>();
		if (isNumeric(strData.getAllele1())) {
			alleleArray.add(getNumeric(strData.getAllele1()));
		} else if (!strData.getAllele1().isEmpty()) {
			alleleArray.add((int)(strData.getAllele1().charAt(0)));
		}
		if (isNumeric(strData.getAllele2())) {
			alleleArray.add(getNumeric(strData.getAllele2()));
		} else if (!strData.getAllele2().isEmpty()) {
			alleleArray.add((int)(strData.getAllele2().charAt(0)));
		}
		if (isNumeric(strData.getAllele3())) {
			alleleArray.add(getNumeric(strData.getAllele3()));
		} else if (!strData.getAllele3().isEmpty()) {
			alleleArray.add((int)(strData.getAllele3().charAt(0)));
		}
		if (isNumeric(strData.getAllele4())) {
			alleleArray.add(getNumeric(strData.getAllele4()));
		} else if (!strData.getAllele4().isEmpty()) {
			alleleArray.add((int)(strData.getAllele4().charAt(0)));
		}
		
		return alleleArray;
	}
	
	private static int getNumeric(String str) {
	    if (str == null) {
	        return 0;
	    }
	    int retVal = 0;
	    try {  
	        retVal = Double.valueOf(str).intValue();
	    } catch (NumberFormatException nfe) { 
	    	if (str.isEmpty()) {
	    		return 0;
	    	} else {
	    		return (int)(str.charAt(0));  
	    	}	
	    }
	    return retVal;
	}
	
	private static boolean isNumeric(String str) {
	    if (str == null)
	        return false;
	    try {  
	        Double.parseDouble(str);
	    } catch (NumberFormatException nfe) {  
	        return false;  
	    }
	    return true;
	}
	
	private static BioSample removeSampleDataWhiteSpace(BioSample sampleData) {
		BioSample newData = new BioSample();
		newData.setSampleDescriptor(trimToEmpty(sampleData.getSampleDescriptor()));
		newData.setModelDisplayName(trimToEmpty(sampleData.getModelDisplayName()));
		newData.setProtocolParticipantID(trimToEmpty(sampleData.getProtocolParticipantID()));
		newData.setPrimaryDiseaseSite(trimToEmpty(sampleData.getPrimaryDiseaseSite()));
		newData.setSampleType(trimToEmpty(sampleData.getSampleType()));
		newData.setInterventionDescription(trimToEmpty(sampleData.getInterventionDescription()));
		newData.setInterventionStartDate(trimToEmpty(sampleData.getInterventionStartDate()));
		newData.setInterventionEndDate(trimToEmpty(sampleData.getInterventionEndDate()));
		newData.setBarcode(trimToEmpty(sampleData.getBarcode()));
		// newData.setCollectionDate(sampleData.getCollectionDate().trim());
		newData.setComment(trimToEmpty(sampleData.getComment()));
		return newData;
	}

	private static STRData removeSTRDataWhiteSpace(STRData strData) {
		STRData newData = new STRData();
		newData.setSampleDescriptor(trimToEmpty(strData.getSampleDescriptor()));
		newData.setReceivingDate(trimToEmpty(strData.getReceivingDate()));
		newData.setMarker(trimToEmpty(strData.getMarker()));
		newData.setPanel(trimToEmpty(strData.getPanel()));
		newData.setAllele1(trimToEmpty(strData.getAllele1()));
		newData.setAllele2(trimToEmpty(strData.getAllele2()));
		newData.setAllele3(trimToEmpty(strData.getAllele3()));
		newData.setAllele4(trimToEmpty(strData.getAllele4()));
		newData.setAllele1n2(trimToEmpty(strData.getAllele1n2()));
		newData.setUploaderName(strData.getUploaderName());
		return newData;
	}

	private static String trimToEmpty(String value) {
		return value == null ? "" : value.trim();
	}

	@Override
	public List<STRData> saveSTRDataList(List<STRData> strDataList, List<STRError> strErrorList) {
		
		List<STRData> retSTRDataList = new ArrayList<STRData>();
		
		// business logic is not fixed so far. 
		// we need to sort the datalist first using a map with key of sample descriptor and receiving date. value is the biomarker count
		HashMap<String, List<STRData>> strListMap = new HashMap<String, List<STRData>>();

		// keep track of who uploaded
		String uploaderName = "";
		for (int i = 0; i < strDataList.size(); i++) {
			STRData rawData = strDataList.get(i);
			if (uploaderName.equals("")) {
				uploaderName = rawData.getUploaderName();
			}

			// remove the extra white space before or after each field
			STRData data = removeSTRDataWhiteSpace(rawData);
			
			// Check if the 2 keys are empty. if so skip this item
			if (data.getSampleDescriptor().isEmpty()) continue;
			if (data.getReceivingDate().isEmpty()) continue;
			if (data.getMarker().isEmpty()) continue;
			
			// Create the key based on sample name and date
			String key = data.getSampleDescriptor() + data.getReceivingDate();
			if (strListMap.containsKey(key)) {
				strListMap.get(key).add(data);
			} else {
				List<STRData> newlist = new ArrayList<STRData>();
				newlist.add(data);
				strListMap.put(key, newlist);
			}
		}
		
		for (String key: strListMap.keySet()) {
			// go through the hashmap to add each STR analysis data
			List<STRData> retList = this.saveSingleSTRAnalysisDataList(strListMap.get(key), strErrorList, uploaderName);
			if (retList != null) {
				retSTRDataList.addAll(retList);
			}
		}
		
		return retSTRDataList;
	}
	
	private List<STRData> saveSingleSTRAnalysisDataList(List<STRData> strDataList, List<STRError> strErrorList, String uploaderName) {

		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("uuuu-MM-dd", Locale.US).withResolverStyle(ResolverStyle.STRICT);;

		List<STRData> retSTRDataList = new ArrayList<STRData>();

		// for now I just check if there exist same sample name and date 
		// if there is, just exit
		STRData data = strDataList.get(0);
		
		if (!data.getReceivingDate().isEmpty()) {
			try {
				dateFormatter.parse(data.getReceivingDate());
			} catch (DateTimeParseException e) {
				STRError strError = new STRError();
				strError.setErrorCode(HttpStatus.CONFLICT.value());
				strError.setErrorSTRSampleID(data.getSampleDescriptor());
				strError.setErrorSTRAnalysisDate(data.getReceivingDate());
				strError.setErrorText(" STR receiving date is invalid. ");
				strErrorList.add(strError);
				return null;
			}
		}

		List<STRData> list = strDataRepository.findBySampleDescriptorAndReceivingDate(data.getSampleDescriptor(), data.getReceivingDate());
		if (list != null && list.size() > 0) {
			// throw new DuplicateEntityFoundException(STRData.class, "Sample Descriptor", data.getSampleDescriptor(), "Receiving Date", data.getReceivingDate());
			STRError strError = new STRError();
			strError.setErrorCode(HttpStatus.CONFLICT.value());
			strError.setErrorSTRSampleID(data.getSampleDescriptor());
			strError.setErrorSTRAnalysisDate(data.getReceivingDate());
			strError.setErrorText(" duplicate STR analysis data found in the database. ");
			strErrorList.add(strError);
			return null;
		}
		
//		// 0.1 sanity check for missing property required sample name/ date/ marker
//		for(int i = 0; i < strDataList.size(); i++ ) {
//			if (strDataList.get(i).getSampleDescriptor().isEmpty()) return null;
//			if (strDataList.get(i).getReceivingDate().isEmpty()) return null;
//			if (strDataList.get(i).getMarker().isEmpty()) return null;
//		}
		
		// 1. need to save the str analysis data 
		STRAnalysis newSTRAnalysis = new STRAnalysis();
		newSTRAnalysis.setSampleDescriptor(data.getSampleDescriptor());
		newSTRAnalysis.setReceivingDate(data.getReceivingDate());
		
		// 2. find the reference biosample
		List<BioSample> biosampleList = biosampleRepository.findBySampleDescriptorIgnoreCase(data.getSampleDescriptor());
		if (biosampleList == null || biosampleList.size() == 0) {
			// throw new EntityNotFoundException(BioSample.class, "Sample Descriptor", data.getSampleDescriptor());
			STRError strError = new STRError();
			strError.setErrorCode(HttpStatus.NOT_FOUND.value());
			strError.setErrorSTRSampleID(data.getSampleDescriptor());
			strError.setErrorSTRAnalysisDate(data.getReceivingDate());
			strError.setErrorText(" reference sample not found in the database. ");
			strErrorList.add(strError);
			return null;
		}
		newSTRAnalysis.setSample(biosampleList.get(0));

		// add uploader info
		newSTRAnalysis.setUploaderName(uploaderName);
		newSTRAnalysis.setOperationTimeStamp(new Timestamp(System.currentTimeMillis()));

		// 3. save the STR analysis
		strAnalysisRepository.save(newSTRAnalysis);

		// 4. need to save the actual STR data
		strDataRepository.saveAll(strDataList).forEach(strData -> retSTRDataList.add(strData));
		return retSTRDataList;
	}
	
	@Override
	public List<STRData> findSTRDataByBiosampleDescriptorAndReceivingDate(String sampleDescriptor, String receivingDate) {
		List<STRData> strList = new ArrayList<STRData>();
		strDataRepository.findBySampleDescriptorAndReceivingDate(sampleDescriptor, receivingDate).forEach(strData -> strList.add(strData));
		return strList;
	}
 
	@Override
	public Study saveStudy(Study study) {
		return studyRepository.save(study);
	}

	@Override
	public List<Study> findAllStudies() {
        List<Study> studyList = new ArrayList<Study>();
        studyRepository.findAll().forEach(study -> studyList.add(study));
        return studyList;
	}
	
	@Override
	public List<Study> findStudiesByShortNameIgnoreCase(String shortname) {
	    List<Study> studyList = new ArrayList<Study>();
	    studyRepository.findByStudyShortNameIgnoreCase(shortname).forEach(study -> studyList.add(study));
	    return studyList;
	}

	@Override
	public List<Study> findStudiesByShortNameIgnoreCaseContaining(String shortname) {
	    List<Study> studyList = new ArrayList<Study>();
	    studyRepository.findByStudyShortNameIgnoreCaseContaining(shortname).forEach(study -> studyList.add(study));
	    return studyList;
	}

	@Override
	public List<Study> findStudiesByPIIgnoreCase(String pi) {
        List<Study> studyList = new ArrayList<Study>();
        studyRepository.findByPrincipleInvestigatorIgnoreCase(pi).forEach(study -> studyList.add(study));
        return studyList;
	}

	@Override
	public List<Study> findStudiesByPIIgnoreCaseContaining(String pi) {
        List<Study> studyList = new ArrayList<Study>();
        studyRepository.findByPrincipleInvestigatorIgnoreCaseContaining(pi).forEach(study -> studyList.add(study));
        return studyList;
	}

 	@Override
	public Study findStudyById(String shortName) {
		Study retStudy = studyRepository.findById(shortName).isPresent()
				? studyRepository.findById(shortName).get()
				: null;
		return retStudy;
	}

	@Override
	public Study updateStudyById(String shortName, Study study) {
		Study retStudy = studyRepository.findById(shortName).isPresent() ? studyRepository.findById(shortName).get(): null;
		if (retStudy != null) {
			retStudy.setStudyShortName(study.getStudyShortName());
			retStudy.setStudyLongName(study.getStudyLongName());
			retStudy.setPrincipleInvestigator(study.getPrincipleInvestigator());
			retStudy.setComment(study.getComment());
			studyRepository.save(retStudy);
		}
		return retStudy;
	}

	@Override
	public Study deleteStudyById(String shortName) {
		Study study = studyRepository.findById(shortName).isPresent() ? studyRepository.findById(shortName).get(): null;
		if (study != null) {
			studyRepository.deleteById(shortName);
		}
		return study;
	}
	
	@Override
	public long getNumberOfStudies() {
		return studyRepository.count();
	}

	@Override
	public Model saveModel(Model model) {
		String modelDisplayName = model.getStudyShortName() + model.getModelIdentifier();
		model.setModelDisplayName(modelDisplayName);
		return modelRepository.save(model);
	}

	@Override
	public List<Model> findAllModels() {
        List<Model> modelList = new ArrayList<Model>();
        modelRepository.findAll().forEach(model -> modelList.add(model));
        return modelList;
	}
	
	@Override
	public List<String> findAllUniquePPID() {
		return biosampleRepository.findAllUniquePPID();
	}
	
	@Override
	public Page<Model> findAllModelsPageable(Pageable pageable) {
		return modelRepository.findAll(pageable);
	}

 	@Override
	public List<Model> findModelsByModelDisplayNameIgnoreCase(String modelDisplayName) {
		List<Model> models = modelRepository.findByModelDisplayNameIgnoreCase(modelDisplayName);
		if (models != null && models.size() > 0) {
			return models;
		} else {
			return null;
		}
	}

 	@Override
 	public List<Model> findModelsByModelDisplayNameIgnoreCaseContaining(String studyId) {
 		List<Model> models = modelRepository.findByModelDisplayNameIgnoreCaseContaining(studyId);
 		if (models != null && models.size() > 0) {
 			return models;
 		} else {
 			return null;
 		}
 	}

	@Override
	public Page<Model> findModelsPageableByModelDisplayNameIgnoreCase(String modelDisplayName, Pageable pageable) {
		return modelRepository.findByModelDisplayNameIgnoreCase(modelDisplayName, pageable);
	}

	@Override
	public Page<Model> findModelsPageableByModelDisplayNameIgnoreCaseContaining(String modelDisplayName, Pageable pageable) {
		return modelRepository.findByModelDisplayNameIgnoreCaseContaining(modelDisplayName, pageable);
	}

 	@Override
	public List<Model> findModelsByPPIdIgnoreCase(String ppid) {
		List<Model> models = modelRepository.findByProtocolParticipantIDIgnoreCase(ppid);
		if (models != null && models.size() > 0) {
			return models;
		} else {
			return null;
		}
	}

 	@Override
	public List<Model> findModelsByPPIdIgnoreCaseContaining(String ppid) {
		List<Model> models = modelRepository.findByProtocolParticipantIDIgnoreCaseContaining(ppid);
		if (models != null && models.size() > 0) {
			return models;
		} else {
			return null;
		}
	}

 	@Override
	public List<Model> findModelsByPPIdList(List<String> ppids) {
		List<Model> models = modelRepository.findByProtocolParticipantIDIn(ppids);
		if (models != null && models.size() > 0) {
			return models;
		} else {
			return null;
		}
	}
 	
 	@Override
	public List<Model> findModelsByPPIdListContaining(List<String> ppids) {
		List<Model> models = modelRepository.findByProtocolParticipantIDIn(ppids);
		if (models != null && models.size() > 0) {
			return models;
		} else {
			return null;
		}
	}

 	@Override
	public Page<Model> findModelsPageableByPPIdList(List<String> ppids, Pageable pageable) {
 		Page<Model> models = modelRepository.findByProtocolParticipantIDIn(ppids, pageable);
		if (models != null && ! models.isEmpty()) {
			return models;
		} else {
			return null;
		}
	}

 	@Override
	public Page<Model> findModelsPageableByPPIdListContaining(List<String> ppids, Pageable pageable) {
 		Page<Model> models = modelRepository.findByProtocolParticipantIDIn(ppids, pageable);
		if (models != null && ! models.isEmpty()) {
			return models;
		} else {
			return null;
		}
	}

 	@Override
	public Page<Model> findModelsPageableByPPIdIgnoreCaseContaining(String ppid, Pageable pageable) {
 		return modelRepository.findByProtocolParticipantIDIgnoreCaseContaining(ppid, pageable);
 	}

 	@Override
 	public List<Model> findModelsByStudyId(String studyId) {
 		List<Model> models = modelRepository.findByStudyShortName(studyId);
 		if (models != null && models.size() > 0) {
 			return models;
 		} else {
 			return null;
 		}
 	}

 	@Override
	public Model findModelByModelDisplayName(String modelDisplayName) {
		List<Model> models = modelRepository.findByModelDisplayName(modelDisplayName);
		if (models != null && models.size() > 0) {
			return models.get(0);
		} else {
			return null;
		}
	}

	@Override
	public Model updateModelByModelDisplayName(String modelDisplayName, Model model) {
		List<Model> models = modelRepository.findByModelDisplayName(modelDisplayName);
		if (models != null && models.size() > 0) {
			Model retModel = models.get(0);
			retModel.setModelDisplayName(model.getModelDisplayName());
			if (!model.getProtocolParticipantID().equalsIgnoreCase(retModel.getProtocolParticipantID())) {
				retModel.setProtocolParticipantID(model.getProtocolParticipantID());
				// MT-141 also need to update the sample table
				List<BioSample> sampleList = biosampleRepository.findByModelDisplayNameIgnoreCase(modelDisplayName);
				if (sampleList != null && sampleList.size() > 0) {
					for(BioSample sample: sampleList) {
						sample.setProtocolParticipantID(model.getProtocolParticipantID());
						biosampleRepository.save(sample);
					}
				}
			} 
			retModel.setParentModelDisplayName(model.getParentModelDisplayName());
			retModel.setComment(model.getComment());
			retModel = modelRepository.save(retModel);
			return retModel;
		} else {
			return null;
		}
	}

	@Override
	public Model deleteModelByModelDisplayName(String modelDisplayName) {
		List<Model> models = modelRepository.findByModelDisplayName(modelDisplayName);
		if (models != null && models.size() == 1) {
			modelRepository.deleteByModelDisplayName(modelDisplayName);
			return models.get(0);
		} else {
			return null;
		}
	}

	@Override
	public long getNumberOfModels() {
		return modelRepository.count();
	}
	
	@Override
	public List<String> findPPIDsByPPIDContaining(String ppid) {
		ppid = "%" + ppid + "%";
		return modelRepository.findPPIDsByProtocolParticipantIDIgnoreCaseContaining(ppid);
	}
	
	@Override
	public long getNumberOfPatients() {
		return modelRepository.countByPPID();
	}

	@Override
	public List<DataFieldOption> findDataFieldOptionsByFieldOptionName(String optionName) {
		List<DataFieldOption> options = null;
		if (optionName.equalsIgnoreCase("all")) {
			options = dataFieldOptionRepository.findAll();
		} else {
			options = dataFieldOptionRepository.findByFieldOptionName(optionName);
		}
		return options;
	}

	@Override
	public List<DataCount> getSTRAnalysisAnalyticsByField(String groupField) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public float getAverageSTRPercentageMatch() {
		return strAnalysisRepository.getAverageSTRPercentageMatch();
	}
	
	public float getAverageSTRPercentageMatchByStudyId(String studyId) {
		return strAnalysisRepository.getAverageSTRPercentageMatchByStudyId(studyId);
	}
	
	public float getAverageSTRPercentageMatchByModelId(String modelId) {
		return strAnalysisRepository.getAverageSTRPercentageMatchByModelId(modelId);
	}
	
	@Override
	public List<DataCount> getSTRPercentageMatchHistogram() {
		List<DataCount> retList = new ArrayList<DataCount>();
		List<Object[]> result = strAnalysisRepository.getSTRPercentageMatchHistogram();
		if(result != null && !result.isEmpty()){
		  for (Object[] object : result) {
			  DataCount count = new DataCount();
			  if (object[0] == null) continue;
			  count.setDataType(Double.toString((Double)object[0]));
			  count.setCount(((BigInteger)object[1]).longValue());
			  retList.add(count);
		  }
	    }
		return retList;
	}
	
	public List<DataCount> getSTRPercentageMatchHistogramByStudyId(String studyId) {
		List<DataCount> retList = new ArrayList<DataCount>();
		studyId = "%" + studyId + "%";
		List<Object[]> result = strAnalysisRepository.getSTRPercentageMatchHistogramByStudyId(studyId);
		if(result != null && !result.isEmpty()){
		  for (Object[] object : result) {
			  DataCount count = new DataCount();
			  if (object[0] == null) continue;
			  count.setDataType(Double.toString((Double)object[0]));
			  count.setCount(((BigInteger)object[1]).longValue());
			  retList.add(count);
		  }
	    }
		return retList;
	}
	
	public List<DataCount> getSTRPercentageMatchHistogramByModelId(String modelId) {
		List<DataCount> retList = new ArrayList<DataCount>();
		List<Object[]> result = strAnalysisRepository.getSTRPercentageMatchHistogramByModelId(modelId);
		if(result != null && !result.isEmpty()){
		  for (Object[] object : result) {
			  DataCount count = new DataCount();
			  if (object[0] == null) continue;
			  count.setDataType(Double.toString((Double)object[0]));
			  count.setCount(((BigInteger)object[1]).longValue());
			  retList.add(count);
		  }
	    }
		return retList;
	}

}
