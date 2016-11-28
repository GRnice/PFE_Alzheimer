angular.module('starter.controllers', ['ionic'])


.controller('ctrlMap',function($scope,$state,$rootScope,$ionicPopup,Socket,Profils)
{

	$scope.up = function()
    {
        Socket.sendMessage("UP","up1");	
    }
	
	setInterval($scope.up,2000);
	
    $scope.visibleMapMenu = true;
    $scope.visibleConfig = false;
    // Visibility
    $scope.showMap = function(){
        $scope.visibleMapMenu = true;
        $scope.visibleConfig = false;
        if(!document.getElementById("mapTab").classList.contains("active")){
          document.getElementById("mapTab").classList.toggle("active");
          document.getElementById("settingsTab").classList.toggle("active");
        }
    };
    
    $scope.showConfig = function(){
        $scope.visibleMapMenu = false;
        $scope.visibleConfig = true;
      if(!document.getElementById("settingsTab").classList.contains("active")){
        document.getElementById("mapTab").classList.toggle("active");
        document.getElementById("settingsTab").classList.toggle("active");
      }
    };

    $scope.profils = Profils.all();
    

    var positions = [];
    positions[0] = new google.maps.LatLng(43.612, 7.08);
	positions[1] = new google.maps.LatLng(43.610, 7.09);


	var mapOptions = {
					  center: positions[0],
					  zoom: 15,
					  mapTypeId: google.maps.MapTypeId.ROADMAP
					};
				
	$scope.map = new google.maps.Map(document.getElementById("map"), mapOptions);
    $scope.cardVisible = [];
    $scope.index = 0;
    $scope.$on('$createPositions',
    function(event, markers){
        console.log(markers);
        for(var i = 0; i < markers.length; i++){
			markers[i].setMap($scope.map);
            google.maps.event.addListener(markers[i], 'click', function () {
                var marker = this;
                $scope.$apply(function () {
                    $scope.nom = Profils.all()[marker.id].nom;
                    $scope.avatar = "img/test.png";
                    $scope.cardVisible[$scope.profils[marker.id].id] = true;
                    for(var n = 0; n < $scope.profils.length; n++){
                        if(n == marker.id){
                            continue;
                        }
                        $scope.cardVisible[$scope.profils[n].id] = false;
                    }
                    console.log($scope.profils);
                });
            });
		}

    });

})

.controller('ctrlSelectProfil',function($scope,$state,$rootScope,$stateParams,Socket,Profils,ProfilSelected)
{
	console.log($stateParams.id);
	$scope.profils = Profils.all();
	
	$scope.profilSelected = function(id)
	{
		var id = parseInt(id);
		var leProfil = Profils.get(id);
		ProfilSelected.set(leProfil);
		Socket.sendMessage("FOLLOW",$stateParams.id+"*"+leProfil.prenom+"*"+leProfil.nom);
        var positions = [];
        var markers = [];
        positions[0] = new google.maps.LatLng(43.612, 7.08);
        positions[1] = new google.maps.LatLng(43.610, 7.09);
        for(var i = 0; i < Profils.all().length; i++){
            console.log("here1");
            var marker = new google.maps.Marker({
                position: positions[i],
                animation: google.maps.Animation.DROP,
				icon: 'img/test.png',
                id: i
            });
            markers[i] = marker;
            // marker.setMap($scope.map);
        }
        $rootScope.$broadcast('$createPositions', markers);
		$state.go("Map");

	}
});