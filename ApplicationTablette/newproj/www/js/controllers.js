angular.module('starter.controllers', ['ionic'])

.controller('ctrlMap', function($scope, $state, $filter,$ionicPopup,Profils)
{
	$scope.socket = io.connect("http://192.168.1.13:2000");
	
	$scope.socket.on("NEWSESSION",function(data)
	{
		console.log(data);
		alert(data);
	})
	
	$scope.up = function()
	{
		$scope.socket.emit("UP",{id : "up"});
	}
	
	setInterval($scope.up,2000);
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

.controller('ctrlSelectProfil',function($scope,$state,$stateParams,Profils,ProfilSelected)
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