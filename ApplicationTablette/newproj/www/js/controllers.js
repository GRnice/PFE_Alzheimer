angular.module('starter.controllers', ['ionic'])


.controller('ctrlMap',function($scope,$state,$rootScope,$ionicPopup,Socket,Profils, ProfilSelected)
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
          document.getElementById("mapTab").classList.add("active");
          document.getElementById("settingsTab").classList.remove("active");
            document.getElementById("profilsTab").classList.remove("active");
        }
    };
    
    $scope.showConfig = function(){
        $scope.visibleMapMenu = false;
        $scope.visibleConfig = true;
      if(!document.getElementById("settingsTab").classList.contains("active")){
        document.getElementById("mapTab").classList.remove("active");
        document.getElementById("settingsTab").classList.add("active");
        document.getElementById("profilsTab").classList.remove("active");
      }
    };

    $scope.showProfils = function(){
        $scope.visibleMapMenu = false;
        $scope.visibleConfig = false;
        if(!document.getElementById("profilsTab").classList.contains("active")){
            document.getElementById("mapTab").classList.remove("active");
            document.getElementById("settingsTab").classList.remove("active");
            document.getElementById("profilsTab").classList.add("active");
        }
        $state.go("SelectProfil", { id: Socket.data() });
    };

    $scope.profils = Profils.all();
    $scope.profilsSelected = [];


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
    function(event, marker){
        $scope.profilsSelected[$scope.profilsSelected.length] = ProfilSelected.get();
        console.log(marker);
        for(var i = 0; i < $scope.profilsSelected.length; i++){
			marker.setMap($scope.map);
            google.maps.event.addListener(marker, 'click', function () {
                $scope.$apply(function () {
                    $scope.nom = ProfilSelected.get().nom;
                    $scope.avatar = "img/test.png";
                    $scope.cardVisible[ProfilSelected.get().id] = true;
                    for(var n = 0; n < $scope.profilsSelected.length; n++){
                        if(n == marker.id){
                            continue;
                        }
                        $scope.cardVisible[ProfilSelected.get().id] = false;
                    }
                    $scope.duree = 90;
                    $scope.batterie = 50;
                    $scope.reseau = "On";
                    console.log(ProfilSelected.get());
                });
            });
		}

		console.log($scope.profilsSelected);
        $scope.showMap();
    });

})
//
// .controller('ctrlSelectProfil',function($scope,$state,$rootScope,$stateParams,Socket,Profils,ProfilSelected)
// {
// 	console.log($stateParams.id);
// 	$scope.profils = Profils.all();
//
// 	$scope.profilSelected = function(id)
// 	{
// 		var id = parseInt(id);
// 		var leProfil = Profils.get(id);
// 		ProfilSelected.set(leProfil);
// 		Socket.sendMessage("FOLLOW",$stateParams.id);
//         var positions = [];
//         var markers = [];
//         positions[0] = new google.maps.LatLng(43.612, 7.08);
//         positions[1] = new google.maps.LatLng(43.610, 7.09);
//         for(var i = 0; i < Profils.all().length; i++){
//             console.log("here1");
//             var marker = new google.maps.Marker({
//                 position: positions[i],
//                 animation: google.maps.Animation.DROP,
// 				icon: 'img/test.png',
//                 id: i
//             });
//             markers[i] = marker;
//             // marker.setMap($scope.map);
//         }
//         $rootScope.$broadcast('$createPositions', markers);
// 		$state.go("Map");
//
// 	}
// });


.controller('ctrlListeProfils',function($scope,$state,$rootScope,$stateParams,Socket,Profils,ProfilSelected)
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
            var marker = new google.maps.Marker({
                position: positions[0],
                animation: google.maps.Animation.DROP,
                icon: 'img/test.png',
                id: ProfilSelected.get().id
            });
            // marker.setMap($scope.map);

        $rootScope.$broadcast('$createPositions', marker);
        $state.go("Map");

    }
});