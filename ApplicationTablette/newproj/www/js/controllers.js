angular.module('starter.controllers', ['ionic'])


.controller('ctrlMap',function($scope,$state,$ionicPopup,Socket,Profils)
{
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
	console.log("COUCOU")
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