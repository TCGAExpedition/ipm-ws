<?php

include_once 'rdfConnector.php';

class sampleSelection {
    //filter queries
	private static $disease_list_Q = NULL;
	private static $tss_list_Q = NULL;
	private static $center_list_Q = NULL;
	private static $sampleType_list_Q = NULL;
	private static $analyteType_list_Q = NULL;
	private static $dataFileType_list_Q = NULL;
	private static $platform_list_Q = NULL;
	private static $level_list_Q = NULl;
	private static $genRef_list_Q = NULl;
	private static $genRefURL_list_Q = NULl;
	
	
	// replaceable filter names in filterPgrrUUID
	private static $f_disease = '<filter_disease>';
	private static $f_tss = '<filter_tss>';
	private static $f_sampleType = '<filter_sampleType>';
	private static $f_analysisDataType = '<filter_analysisDataType>';
	private static $f_platform = '<filter_platform>';
	private static $f_level = '<filter_level>';
	private static $f_analyteType = '<filter_analyte>';
	private static $f_center = '<filter_center>';
	private static $f_genomeRef = '<genomeRefName_Line>';
	
	
	//data
	private static $all_pgrrUUIDList_Q = NULL;
	private static $filter_pgrrUUIDList_Q = NULL;
	private static $data_by_pgrrUUIDList_Q = NULL;
	private static $get_filePaths_Q = NULL;

	
	public static function diseaseList(){
		try{
	echo 'Q: '.self::$disease_list_Q.PHP_EOL;
			return rdfConnector::execQuery(self::$disease_list_Q);
		}catch (Exception $e){
			echo 'sampleSelection.diseaseList: ',  $e->getMessage(), "\n";
			throw new Exception( 'sampleSelection.diseaseList: ', 0, $e);
		}
	}
	
	public static function tssList(){
		try{
			echo 'Q: '.self::$tss_list_Q.PHP_EOL;
			return rdfConnector::execQuery(self::$tss_list_Q);
		}catch (Exception $e){
			echo 'sampleSelection.tssList: ',  $e->getMessage(), "\n";
			throw new Exception( 'sampleSelection.tssList: ', 0, $e);
		}
	}
	
	public static function centerList(){
		try{
			echo 'Q: '.self::$center_list_Q.PHP_EOL;
			return rdfConnector::execQuery(self::$center_list_Q);
		}catch (Exception $e){
			echo 'sampleSelection.centerList: ',  $e->getMessage(), "\n";
			throw new Exception( 'sampleSelection.centerList: ', 0, $e);
		}
	}
	
	public static function sampleTypeList(){
		try{
			echo 'Q: '.self::$sampleType_list_Q.PHP_EOL;
			return rdfConnector::execQuery(self::$sampleType_list_Q);
		}catch (Exception $e){
			echo 'sampleSelection.sampleTypeList: ',  $e->getMessage(), "\n";
			throw new Exception( 'sampleSelection.sampleTypeList: ', 0, $e);
		}
	}
	
	public static function analyteTypeList(){
		try{
			echo 'Q: '.self::$analyteType_list_Q.PHP_EOL;
			return rdfConnector::execQuery(self::$analyteType_list_Q);
		}catch (Exception $e){
			echo 'sampleSelection.analyteTypeList: ',  $e->getMessage(), "\n";
			throw new Exception( 'sampleSelection.analyteTypeList: ', 0, $e);
		}
	}
	
	
	/**
	 *
	 * @throws Exception
	 * @return JSON
	 */
	public static function dataFileTypeList(){
		try{
			echo 'Q: '.self::$dataFileType_list_Q.PHP_EOL;
			return rdfConnector::execQuery(self::$dataFileType_list_Q);
		}catch (Exception $e){
			echo 'sampleSelection.dataFileTypeList: ',  $e->getMessage(), "\n";
			throw new Exception( 'sampleSelection.dataFileTypeList: ', 0, $e);
		}
	}
	
	/**
	 * Buggy data: possible "-" or "_" as separators => additional same platform
	 * @throws Exception
	 * @return string
	 */
	public static function platformList(){
		try{
			echo 'Q: '.self::$platform_list_Q.PHP_EOL;
			$json = rdfConnector::execQuery(self::$platform_list_Q);
			
			// separators to space
			$json = str_replace('-', ' ', $json);
			$json = str_replace('_', ' ', $json);
			// remove duplicates
			$res = array();
			foreach(preg_split("/((\r?\n)|(\r\n?))/", $json) as $line){
				if (!in_array($line, $res))
					array_push($res, $line);
			}
			
			$json = implode("\n", $res);
			
			return $json;
		}catch (Exception $e){
			echo 'sampleSelection.platformList: ',  $e->getMessage(), "\n";
			throw new Exception( 'sampleSelection.platformList: ', 0, $e);
		}
	}
	
	public static function levelList(){
		try{
			echo 'Q: '.self::$level_list_Q.PHP_EOL;
			return rdfConnector::execQuery(self::$level_list_Q);
		}catch (Exception $e){
			echo 'sampleSelection.levelList: ',  $e->getMessage(), "\n";
			throw new Exception( 'sampleSelection.levelList: ', 0, $e);
		}	 
	}
	
	public static function genRefList(){
		try{
			echo 'Q: '.self::$genRef_list_Q.PHP_EOL;
			return rdfConnector::execQuery(self::$genRef_list_Q);
		}catch (Exception $e){
			echo 'sampleSelection.genRefList: ',  $e->getMessage(), "\n";
			throw new Exception( 'sampleSelection.genRefList: ', 0, $e);
		}
	}
	
	public static function genRefURLList(){
		try{
			echo 'Q: '.self::$genRefURL_list_Q.PHP_EOL;
			return rdfConnector::execQuery(self::$genRefURL_list_Q);
		}catch (Exception $e){
			echo 'sampleSelection.genRefURLList: ',  $e->getMessage(), "\n";
			throw new Exception( 'sampleSelection.genRefURLList: ', 0, $e);
		}
	}
	
	
	
	public static function allPgrrUUIDList(){
		try{
			$q = str_replace('<dateTime>', date('Y-m-d'), self::$all_pgrrUUIDList_Q);
			echo 'Q: '.$q.PHP_EOL;
			return rdfConnector::execQuery($q);
		}catch (Exception $e){
			echo 'sampleSelection.allPgrrUUIDList: ',  $e->getMessage(), "\n";
			throw new Exception( 'sampleSelection.allPgrrUUIDList: ', 0, $e);
		}
	}
	
	/**
	 * 
	 * @param array $disease
	 * @param array $tss
	 * @param array $sampleType
	 * @param array $dataFileType
	 * @param array $platform
	 * @param array $level
	 * @param array $analyteType
	 * @param array $center
	 * @param array $genRef
	 * @return JSON
	 */
	public static function filterCurrentPgrrUUID($disease, $tss, $sampleType, $dataFileType, $platform, 
			$level, $analyteType, $center, $genRef){
		return self::filterPgrrUUID($disease, $tss, $sampleType, $dataFileType, $platform, $level, $analyteType, $center, $genRef, date('Y-m-d'));
	}
	
	/**
	 * Direct call when historical records, for current set call filterCurrentPgrrUUID(...) 
	 * @param array $disease
	 * @param array $tss
	 * @param array $sampleType
	 * @param array $dataFileType
	 * @param array $platform
	 * @param array $level
	 * @param array $analyteType
	 * @param array $center
	 * @param array $genRef
	 * @param date $date - in format YYYY-MM-DD. File creation date
	 * @throws Exception
	 * @return JSON
	 */
	public static function filterPgrrUUID($disease, $tss, $sampleType, $dataFileType, $platform, 
			$level, $analyteType, $center, $genRef, $date){	
		try{
			$q = str_replace('<dateTime>', date('Y-m-d'), self::$filter_pgrrUUIDList_Q);
			$hasFilter = false;
			if($disease != NULL){
				$hasFilter = true;
				$q = self::replaceByFilter(self::$f_disease, array("?diseaseAbbr"), $disease, NULL, $q);
			} else $q = str_replace(self::$f_disease, '', $q);
			
			if($tss != NULL){
				$hasFilter = true;
				$q = self::replaceByFilter(self::$f_tss, array("?tssName"), $tss, NULL, $q);
			}else $q = str_replace(self::$f_tss, '', $q);
			
			if($sampleType != NULL){
				$hasFilter = true;
				$q = self::replaceByFilter(self::$f_sampleType, array("?sampleTypeDesc"), $sampleType, NULL, $q);
			}else $q = str_replace(self::$f_sampleType, '', $q);
			
			if($dataFileType != NULL){
				$hasFilter = true;
				$q = self::replaceByFilter(self::$f_analysisDataType, array("?analysisType", "?dataType"), $dataFileType, ": ", $q);
			}else $q = str_replace(self::$f_analysisDataType, '', $q);
			
			if($platform != NULL){
				$hasFilter = true;
				// buggy TCGA data: space as word separator could be '-' OR '_'
				// recreate the list 
				$newP = array();
				foreach($platform as $pl){
					if(strpos($pl, ' ') !== FALSE) {
						array_push($newP, str_replace(' ', '-',$pl));
						array_push($newP, str_replace(' ', '_',$pl));
					} else array_push($newP,$pl);
				}
				$q = self::replaceByFilter(self::$f_platform, array("?platform"), $newP, NULL, $q);
			}else $q = str_replace(self::$f_platform, '', $q);
			
			if($level != NULL){
				$hasFilter = true;
				$q = self::replaceByFilter(self::$f_level, array("?level"), $level, NULL, $q);
			}else $q = str_replace(self::$f_level, '', $q);
			
			if($analyteType != NULL){
				$hasFilter = true;
				$q = self::replaceByFilter(self::$f_analyteType, array("?analyteDesc"), $analyteType, NULL, $q);
			}else $q = str_replace(self::$f_analyteType, '', $q);
			
			if($center != NULL){
				$hasFilter = true;
				$q = self::replaceByFilter(self::$f_center, array("?centerName"), $center, NULL, $q);
			}else $q = str_replace(self::$f_center, '', $q);
			
			if($genRef != NULL){
				$hasFilter = true;
				$ref = "OPTIONAL { ?s pgrr:refGenomeName ?rg . FILTER ( ?rg IN(".self::arrToQuotedStr($genRef).")) } .";
				$q = str_replace(self::$f_genomeRef, $ref, $q);
			}else $q = str_replace(self::$f_genomeRef, '', $q);
			
			if(!$hasFilter)
				$q = self::$all_pgrrUUIDList_Q;
			
			
			echo 'Q: '.$q.PHP_EOL;
			return rdfConnector::execQuery($q);
		}catch (Exception $e){
			echo 'sampleSelection.filterPgrrUUID: ',  $e->getMessage(), "\n";
			throw new Exception( 'sampleSelection.filterPgrrUUID: ', 0, $e);
		}
	
	}
	
	/**
	 * Q: how to pass array by reference and unset it in arrToQuotedStr??
	 * @param array $pgrrUUIDArr
	 * @throws Exception
	 * @return JSON
	 */
	public static function dataByPgrrUUIDArr($pgrrUUIDArr){
		try{
			$filter = 'FILTER (?pgrrUUID IN('.self::arrToQuotedStr($pgrrUUIDArr).'))';
			$q = str_replace('<filter_pgrrUUIDList>', $filter, self::$data_by_pgrrUUIDList_Q);
			echo 'Q: '.$q.PHP_EOL;
			return rdfConnector::execQuery($q);
		}catch (Exception $e){
			echo 'sampleSelection.dataByPgrrUUIDArr: ',  $e->getMessage(), "\n";
			throw new Exception( 'sampleSelection.dataByPgrrUUIDArr: ', 0, $e);
		}
	}
	
	public static function getPaths($pgrrUUIDArr){
		try{
			$q = str_replace('<pgrrUUID_List>', self::arrToQuotedStr($pgrrUUIDArr), self::$get_filePaths_Q);
			echo 'Q: '.$q.PHP_EOL;
			return rdfConnector::execQuery($q);
		}catch (Exception $e){
			echo 'sampleSelection.getPaths: ',  $e->getMessage(), "\n";
			throw new Exception( 'sampleSelection.getPaths: ', 0, $e);
		}
	}
	

	private static function arrToQuotedStr(&$arr){
		$str = "";
		foreach ($arr as $val)
			$str .= "\"".$val."\",";			
		$arr = NULL;
		return rtrim($str, ",");
	}
	
	/**
	 * 
	 * @param string $filterName - placeholder name in query template
	 * @param array $nameArr - names for filter (min =1, max = 2)
	 * @param array $valArr - values for filter 
	 * @param string $combinedAs - ': ' used for <AnalysisType>: <dataType> combination, otherwise == null;
	 * @param string $query - where to replace
	 */
	private static function replaceByFilter($filterName, $nameArr, $valArr, $combinedAs, $query){
		$filter = " FILTER (";
		if($combinedAs != NULL){
			foreach ($valArr as $val){
				$ar = explode(": ", $val);
				$filter .= "(".$nameArr[0]."=\"".$ar[0]."\" && ".$nameArr[1]."= \"".$ar[1]."\") || ";
			}
			$filter = rtrim($filter, " || ");
		} else 
			$filter .= $nameArr[0]." IN(".self::arrToQuotedStr($valArr).")";
		$filter .= ")";	
		return str_replace($filterName, $filter, $query);
	}
	
	
	private static function initQueries(){
		$file = "../../../resources/queryVirt.ini";
		$qf = parse_ini_file($file, true);
		//common
		$prefix_name = $qf['common']['prefix_name'];
		$prefix = $qf['common']['prefix'];
		$storage_path = $qf['common']['storage_path'];
	    // filters
		self::$disease_list_Q = self::queryInitReplace($qf['disease_list'],$prefix_name, $prefix);
		self::$tss_list_Q = self::queryInitReplace($qf['tss_list'],$prefix_name, $prefix);
		self::$center_list_Q = self::queryInitReplace($qf['center_list'],$prefix_name, $prefix);
		self::$sampleType_list_Q = self::queryInitReplace($qf['sampleType_list'],$prefix_name, $prefix);
		self::$analyteType_list_Q = self::queryInitReplace($qf['analyteType_list'],$prefix_name, $prefix);
		self::$dataFileType_list_Q = self::queryInitReplace($qf['fileType_by_dataType'],$prefix_name, $prefix);
		self::$platform_list_Q = self::queryInitReplace($qf['platform_list'],$prefix_name, $prefix);
		self::$level_list_Q = self::queryInitReplace($qf['level_list'],$prefix_name, $prefix);
		self::$genRef_list_Q = self::queryInitReplace($qf['genRef_list'],$prefix_name, $prefix);
		self::$genRefURL_list_Q = self::queryInitReplace($qf['genRefURL_list'],$prefix_name, $prefix);
		
		// data
		self::$all_pgrrUUIDList_Q = self::queryInitReplace($qf['all_pgrrUUIDList'],$prefix_name, $prefix);
		self::$filter_pgrrUUIDList_Q = self::queryInitReplace($qf['filter_pgrrUUIDList'],$prefix_name, $prefix);
		self::$data_by_pgrrUUIDList_Q = self::queryInitReplace($qf['data_by_pgrrUUIDList'],$prefix_name, $prefix);
		self::$data_by_pgrrUUIDList_Q = str_replace('<storage_path>', $storage_path, self::$data_by_pgrrUUIDList_Q);
		$r = self::queryInitReplace($qf['get_filePaths'],$prefix_name, $prefix);
		self::$get_filePaths_Q = str_replace('<storage_path>', $storage_path, $r);
	}
	
	
	private static function iniArrayToStr($q_arr){
		$toret = null;
		foreach($q_arr as $val)
			$toret = implode("",$val);
		return $toret;
	}
	
	private static function queryInitReplace($q_arr, $prefix_name, $prefix){
		$toret = self::iniArrayToStr($q_arr);
		$toret = str_replace('<prefix_name>', $prefix_name, $toret);
		return str_replace('<prefix>', $prefix, $toret);
	}

	function main(){
		self::initQueries();
		
		
		$st_time = time();
		$res = sampleSelection::diseaseList();
		//$res = sampleSelection::tssList();
		//$res = sampleSelection::centerList();
		//$res = sampleSelection::sampleTypeList();
		//$res = sampleSelection::analyteTypeList();
		//$res = sampleSelection::dataFileTypeList();
		//$res = sampleSelection::platformList();
		//$res = sampleSelection::levelList();
		//$res = sampleSelection::genRefList();
		//$res = sampleSelection::genRefURLList();
		$res = sampleSelection::allPgrrUUIDList();
		
		
		//--- applying filters ---//
		//$res = sampleSelection::filterCurrentPgrrUUID(null,null,null,null,null,null,null,null);
		//filterPgrrUUID($disease, $tss, $sampleType, $dataFileType, $platform, $level, $analyteType, $center)
		$disease = array("brca", "blca");
		//$res = sampleSelection::filterCurrentPgrrUUID($disease,null,null,null,null,null,null,null,null);
		$tss = array("University of Pittsburgh");
		//$res = sampleSelection::filterCurrentPgrrUUID(null,$tss,null,null,null,null,null,null,null);
		$sampleType = array("Primary solid Tumor", "Solid Tissue Normal");
		//$res = sampleSelection::filterCurrentPgrrUUID(null,null,$sampleType,null,null,null,null,null,null);
		$dataFileType = array("Clinical: patient", "Clinical: sample", "RNASeq: spljxn");
		//$res = sampleSelection::filterCurrentPgrrUUID(null,null,null,$dataFileType,null,null,null,null,null);
		$platform = array("biotab", "IlluminaHiSeq RNASeqV2", "IlluminaGA RNASeq");
		//$res = sampleSelection::filterCurrentPgrrUUID(null,null,null,null,$platform,null,null,null,null);
		$level = array("3");
		//$res = sampleSelection::filterCurrentPgrrUUID(null,null,null,null,null,$level,null,null,null);
		$analyteType = array("Total RNA", "mirVana RNA (Allprep DNA) produced by hybrid protocol");
		//$res = sampleSelection::filterCurrentPgrrUUID(null,null,null,null,null,null,$analyteType,null,null);
		$center = array("hms.harvard.edu", "genome.wustl.edu");
		//$res = sampleSelection::filterCurrentPgrrUUID(null,null,null,null,null,null,null,$center,null);
		$genRef = array("hg18", "hg19");
		//$res = sampleSelection::filterCurrentPgrrUUID($disease,null,null,null,null,null,null,null,$genRef);
		//$res = sampleSelection::filterCurrentPgrrUUID($disease,null,null,null,null,null,$analyteType,null,null);
		
		
		$uuids = array("59ce4c5f-430c-4723-bb93-49d3e767ff16","f0395e71-b648-45b2-a390-0a2274ac68cf","6b79a43e-75d3-4091-aeb6-5c89bbb91754");
		
		//--- data by uuid list ---//
		//$res = sampleSelection::dataByPgrrUUIDArr($uuids);
		
		//--- paths ---//
		//$res = sampleSelection::getPaths($uuids);
		
		$end_time = time();
		
		//echo 'res: '.$res."\n";
		echo 'exec time: '.($end_time - $st_time)." sec".PHP_EOL;
	}
}

sampleSelection::main();

?>