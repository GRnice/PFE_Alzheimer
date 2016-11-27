angular.module('starter.controllers', ['ionic'])


.controller('ctrlMap',function($scope,$state,$ionicPopup,Socket,Profils)
{
    $scope.visibleMapMenu = true;
    $scope.visibleConfig = false;
    // Visibility
    $scope.showMap = function(){
      $scope.visibleMapMenu = true;
      $scope.visibleConfig = false;
      document.getElementById("mapTab").classList.toggle("active");
      document.getElementById("settingsTab").classList.toggle("active");
    };
    
    $scope.showConfig = function(){
      $scope.visibleMapMenu = false;
      $scope.visibleConfig = true;
        document.getElementById("mapTab").classList.toggle("active");
        document.getElementById("settingsTab").classList.toggle("active");
    };
    
    $scope.profils = Profils.all();
    
	var latLng = new google.maps.LatLng(43.612, 7.08);
	var mapOptions = {
					  center: latLng,
					  zoom: 15,
					  mapTypeId: google.maps.MapTypeId.ROADMAP
					};
				
	$scope.map = new google.maps.Map(document.getElementById("map"), mapOptions);
	
	
	var marker = new google.maps.Marker({
		position: latLng,
		nom: "BurgerExpress d'Australie",
		adresse: "45 avenue du kangourou",
		map: $scope.map,
		animation: google.maps.Animation.DROP
	});
	
	marker.setMap($scope.map);
	
})

.controller('ctrlSelectProfil',function($scope,$state,$stateParams,Socket,Profils,ProfilSelected)
{
	console.log("COUCOU");
	console.log($stateParams.id);
	$scope.profils = Profils.all();
	
	$scope.profilSelected = function(id)
	{
		var id = parseInt(id);
		var leProfil = Profils.get(id);
		ProfilSelected.set(leProfil);
		Socket.sendMessage("FOLLOW",$stateParams.id);
		$state.go("Map");
	}
});