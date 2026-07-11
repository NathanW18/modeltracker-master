package ca.uhn.cip.modeltracker.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import ca.uhn.cip.modeltracker.model.BioSample;
import ca.uhn.cip.modeltracker.model.DataCount;
import ca.uhn.cip.modeltracker.model.DataFieldOption;
import ca.uhn.cip.modeltracker.model.Study;
import ca.uhn.cip.modeltracker.model.Model;
import ca.uhn.cip.modeltracker.model.STRAnalysis;
import ca.uhn.cip.modeltracker.model.STRData;
import ca.uhn.cip.modeltracker.model.STRError;
import ca.uhn.cip.modeltracker.model.STRMatch;

/**
 * Spring boot service interface for biosample.
 * 
 * @author Kevin Wang
 *
 */

public interface BioSampleService {
	
	public abstract Study saveStudy(Study study);
	public abstract List<Study> findAllStudies();
	public abstract List<Study> findStudiesByShortNameIgnoreCase(String shortname);
	public abstract List<Study> findStudiesByShortNameIgnoreCaseContaining(String shortname);
	public abstract List<Study> findStudiesByPIIgnoreCase(String pi);
	public abstract List<Study> findStudiesByPIIgnoreCaseContaining(String shortname);
	public abstract Study findStudyById(String id);
	public abstract Study updateStudyById(String id, Study study);
	public abstract Study deleteStudyById(String id);
	public abstract long getNumberOfStudies();

	public abstract Model saveModel(Model model);
	public abstract List<Model> findAllModels();
	public abstract List<String> findAllUniquePPID();
	// public abstract Model findModelByIdAndStudyName(String id, String studyName);
	public abstract List<Model> findModelsByModelDisplayNameIgnoreCase(String modelDisplayName);
	public abstract List<Model> findModelsByModelDisplayNameIgnoreCaseContaining(String modelDisplayName);
	public abstract Model findModelByModelDisplayName(String modelDisplayName);
	public abstract List<Model> findModelsByPPIdIgnoreCase(String ppid);
	public abstract List<Model> findModelsByPPIdIgnoreCaseContaining(String ppid);
	public abstract List<Model> findModelsByPPIdList(List<String> ppids);
	public abstract List<Model> findModelsByPPIdListContaining(List<String> ppids);
	public abstract List<Model> findModelsByStudyId(String studyId);
	public abstract Model updateModelByModelDisplayName(String modelDisplayName, Model model);
	public abstract Model deleteModelByModelDisplayName(String modelDisplayName);
	public abstract long getNumberOfModels();
	
	public abstract long getNumberOfPatients();
	
	public abstract List<String> findPPIDsByPPIDContaining(String ppid);
	
	public abstract BioSample saveBioSample(BioSample sample);
	public abstract List<BioSample> saveBioSampleList(List<BioSample> sampleList, List<STRError> strErrorList);
	public abstract List<BioSample> findAllBioSamples();
	public abstract List<BioSample> findBioSamplesBySampleIdIgnoreCase(String sampleId);
	public abstract List<BioSample> findBioSamplesBySampleIdIgnoreCaseContaining(String sampleId);
	public abstract List<BioSample> findBioSamplesByModelIdIgnoreCase(String modelId);
	public abstract List<BioSample> findBioSamplesByModelIdIgnoreCaseContaining(String modelId);
	public abstract List<BioSample> findBioSamplesByPPIdIgnoreCase(String ppid);
	public abstract List<BioSample> findBioSamplesByPPIdIgnoreCaseContaining(String modelId);
	public abstract List<BioSample> findBioSamplesByPPIdList(List<String> ppids);
	public abstract List<BioSample> findBioSamplesByPPIdListContaining(List<String> ppids);
	public abstract BioSample findBioSampleById(Long id);
	public abstract BioSample updateBioSampleById(Long id, BioSample biosample);
	public abstract BioSample deleteBioSampleById(Long id);
	public abstract long getNumberOfBioSamples();
	public abstract List<DataCount> getBioSampleAnalyticsByField(String groupField);

	public abstract STRAnalysis saveSTRAnalysis(STRAnalysis strAnalysis);
	public abstract List<STRAnalysis> findAllSTRAnalyses();
	public abstract List<STRAnalysis> findAllReferenceSTRAnalyses();
	public abstract List<STRAnalysis> findSTRAnalysesBySampleIdIgnoreCase(String sampleId);
	public abstract List<STRAnalysis> findSTRAnalysesBySampleIdIgnoreCaseContaining(String sampleId);
	public abstract List<STRAnalysis> findSTRAnalysesByModelIdIgnoreCase(String modelId);
	public abstract List<STRAnalysis> findSTRAnalysesByModelIdIgnoreCaseContaining(String modelId);
	public abstract List<STRAnalysis> findSTRAnalysesByPPIdIgnoreCase(String ppid);
	public abstract List<STRAnalysis> findSTRAnalysesByPPIdIgnoreCaseContaining(String ppid);
	public abstract List<STRAnalysis> findSTRAnalysesByPPIdList(List<String> ppids);
	public abstract List<STRAnalysis> findSTRAnalysesByPPIdListContaining(List<String> ppids);
	public abstract List<STRAnalysis> findSTRAnalysesBySampleDescriptor(String sampleDescriptor);
	public abstract List<STRAnalysis> findSTRAnalysesBySTRDataList(List<STRData> strDataList);
	public abstract STRAnalysis findRefSTRAnalysisBySTRAnalysis(STRAnalysis strAnalysis);
	public abstract STRAnalysis findSTRAnalysisBySampleDescriptorAndReceivingDate(String sampleDescriptor, String receivingDate);
	public abstract STRAnalysis updateSTRAnalysisBySampleDescriptorAndReceivingDate(String sampleDescriptor, String receivingDate, STRAnalysis strAnalysis);
	public abstract STRAnalysis deleteSTRAnalysisBySampleDescriptorAndReceivingDate(String sampleDescriptor, String receivingDate);
	public abstract long getNumberOfSTRAnalyses();
	public abstract List<DataCount> getSTRAnalysisAnalyticsByField(String groupField);
	
	public abstract STRMatch computeSTRMatch(List<STRData> strDataRef, List<STRData> strDataSrc);
	
	public abstract List<STRData> saveSTRDataList(List<STRData> strDataList, List<STRError> strErrorList);
	public abstract List<STRData> findSTRDataByBiosampleDescriptorAndReceivingDate(String sampleId, String receivingDate);

	public abstract List<DataFieldOption> findDataFieldOptionsByFieldOptionName(String optionName);
	
	// pagination
	public abstract Page<Model> findAllModelsPageable(Pageable pageable);
	public abstract Page<Model> findModelsPageableByModelDisplayNameIgnoreCase(String modelDisplayName, Pageable pageable);
	public abstract Page<Model> findModelsPageableByModelDisplayNameIgnoreCaseContaining(String modelDisplayName, Pageable pageable);
	public abstract Page<Model> findModelsPageableByPPIdIgnoreCaseContaining(String ppid, Pageable pageable);
	public abstract Page<Model> findModelsPageableByPPIdList(List<String> ppids, Pageable pageable);
	public abstract Page<Model> findModelsPageableByPPIdListContaining(List<String> ppids, Pageable pageable);
	
	public abstract Page<BioSample> findAllBioSamplesPageable(Pageable pageable);
	public abstract Page<BioSample> findBioSamplesPageableBySampleIdIgnoreCase(String sampleId, Pageable pageable);
	public abstract Page<BioSample> findBioSamplesPageableBySampleIdIgnoreCaseContaining(String sampleId, Pageable pageable);
	public abstract Page<BioSample> findBioSamplesPageableByModelIdIgnoreCase(String modelId, Pageable pageable);
	public abstract Page<BioSample> findBioSamplesPageableByModelIdIgnoreCaseContaining(String modelId, Pageable pageable);
	public abstract Page<BioSample> findBioSamplesPageableByPPIdIgnoreCaseContaining(String ppid, Pageable pageable);
	public abstract Page<BioSample> findBioSamplesPageableByPPIdList(List<String> ppids, Pageable pageable);
	public abstract Page<BioSample> findBioSamplesPageableByPPIdListContaining(List<String> ppids, Pageable pageable);
	
	public abstract Page<STRAnalysis> findAllSTRAnalysesPageable(Pageable pageable);
	public abstract Page<STRAnalysis> findSTRAnalysesPageableBySampleIdIgnoreCase(String sampleId, Pageable pageable);
	public abstract Page<STRAnalysis> findSTRAnalysesPageableBySampleIdIgnoreCaseContaining(String sampleId, Pageable pageable);
	public abstract Page<STRAnalysis> findSTRAnalysesPageableByModelIdIgnoreCase(String modelId, Pageable pageable);
	public abstract Page<STRAnalysis> findSTRAnalysesPageableByModelIdIgnoreCaseContaining(String modelId, Pageable pageable);
	public abstract Page<STRAnalysis> findSTRAnalysesPageableByPPIdIgnoreCaseContaining(String ppid, Pageable pageable);
	public abstract Page<STRAnalysis> findSTRAnalysesPageableByPPIdList(List<String> ppids, Pageable pageable);
	public abstract Page<STRAnalysis> findSTRAnalysesPageableByPPIdListContaining(List<String> ppids, Pageable pageable);

	// analytics
	public abstract float getAverageSTRPercentageMatch();
	public abstract float getAverageSTRPercentageMatchByStudyId(String studyId);
	public abstract float getAverageSTRPercentageMatchByModelId(String modelId);
	public abstract List<DataCount> getSTRPercentageMatchHistogram();
	public abstract List<DataCount> getSTRPercentageMatchHistogramByStudyId(String studyId);
	public abstract List<DataCount> getSTRPercentageMatchHistogramByModelId(String modelId);
	
}
