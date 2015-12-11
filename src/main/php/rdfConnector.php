<?php

require ('../../../lib/composer/vendor/nategood/httpful/bootstrap.php');
use \Httpful\Request;

ini_set("memory_limit", "1024M");
date_default_timezone_set('America/New_York');


final class rdfConnector {
	private static $sparql_endpoint = NULL;
	private static $update_endpoint = NULL;
	
	public static function execQuery($query){
		try{
			$qEndPoint = self::sparqlEndpoint();
			
			$uri = $qEndPoint.urlencode($query);
			$json = Request::post($uri)->addHeader("Accept","application/sparql-results+json")->expectsType('json')->send();
			return $json;
		}catch (Exception $e){
			echo 'rdfConnctor.execQuery: ',  $e->getMessage(), "\n";
 			throw new Exception( 'rdfConnctor.execQuery: ', 0, $e);
		}
	}
	
	public static function sparqlEndpoint(){
		return self::$sparql_endpoint;
	}
	
	public static function updateEndpoint(){
		return self::$update_endpoint;
	}
	
	private static function initEndPoints(){
		$file = "../../../resources/configVirt.ini";
		$rdf = parse_ini_file($file, true);
		self::$sparql_endpoint = $rdf['store']['rdf_protocol'].
		$rdf['store']['rdf_host'].':'.
		$rdf['store']['rdf_port'].
		$rdf['sparql']['endpoint']."?query=";
	}
	
	function main(){
		self::initEndPoints();
		//print_r($rdf);
		echo 'IN MAIN sparqlEndpoint: '.rdfConnector::sparqlEndpoint()."\n";
		//res: '.var_dump($rdf[sparql]['headerTypes'])."\n";
	}
}

rdfConnector::main();

?>
