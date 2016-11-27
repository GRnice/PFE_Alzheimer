angular.module('starter.controllers', ['ionic'])


.controller('ctrlMap',function($scope,$state,$rootScope,$ionicPopup,Socket,Profils)
{
    // Visibility
    $scope.showMap = function(){
      document.getElementById("mapTab").classList.toggle("active");
      document.getElementById("settingsTab").classList.toggle("active");
    };
    
    $scope.showConfig = function(){
        document.getElementById("mapTab").classList.toggle("active");
        document.getElementById("settingsTab").classList.toggle("active");
    };
    
    var positions = [];
    positions[0] = new google.maps.LatLng(43.612, 7.08);
	positions[1] = new google.maps.LatLng(43.610, 7.09);
	var mapOptions = {
					  center: positions[0],
					  zoom: 15,
					  mapTypeId: google.maps.MapTypeId.ROADMAP
					};
				
	$scope.map = new google.maps.Map(document.getElementById("map"), mapOptions);

    $scope.$on('$createPositions',
    function(event, markers){
    	for(var i = 0; i < markers.length; i++){
			console.log(markers);
			markers[i].setMap($scope.map);
		}


    });

})

.controller('ctrlSelectProfil',function($scope,$state,$rootScope,$stateParams,Socket,Profils,ProfilSelected)
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
        var positions = [];
        var markers = [];
        positions[0] = new google.maps.LatLng(43.612, 7.08);
        positions[1] = new google.maps.LatLng(43.610, 7.09);
        for(var i = 0; i < Profils.all().length; i++){
            console.log("here1");
            var marker = new google.maps.Marker({
                position: positions[i],
                animation: google.maps.Animation.DROP,
				icon: 'img/test.png'
            });
            markers[i] = marker;
            // marker.setMap($scope.map);
        }
        $rootScope.$broadcast('$createPositions', markers);
		$state.go("Map");

	}
});