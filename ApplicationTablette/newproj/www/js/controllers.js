angular.module('starter.controllers', ['ionic'])


.controller('ctrlMap',function($scope,$state,$rootScope,$ionicPopup,Socket,Profils, ProfilSelected,Tels)
{

    $scope.$on('$notifSession',
        function(event, notifBool){
            // A confirm dialog
            $scope.showConfirm = function() {
                var confirmPopup = $ionicPopup.confirm({
                title: 'Appairage',
                template: 'Voulez-vous ajouter le smartphone ...'
            });

            confirmPopup.then(function(res) {
                if(res) {
                    console.log('OK');
                    $state.go("SelectProfil", { idTel: Socket.data() });
                  Tels.add(Socket.data());
                  Tels.set(Socket.data());

                } else {
                    console.log('Cancel');
                    }
                });

        };
        if(notifBool) {
            $scope.showConfirm();
        }

    });

    $scope.$on('$updatePositions',
        function(event, liste){
            var idTel = liste[0];
            console.log("idTel1");
            console.log(idTel);
            var position = {
                longitude : liste[1],
                latitude : liste[2]
            };
            for(var i = 0; i < $scope.profilsSelected.length; i++){
                console.log("idTel2");
                console.log($scope.profilsSelected[i].idTel);
                if($scope.profilsSelected[i].idTel === idTel){
                    $scope.profilsSelected[i].position = position;
                    console.log($scope.profilsSelected[i]);
                }
            }
            console.log("LAT");
            console.log(position.latitude);
            console.log("LONG");
            console.log(position.longitude);
            console.log("Markers");
            console.log($scope.markers);
            for(i = 0; i < $scope.markers.length; i++){
                if($scope.profilsSelected[i].id === $scope.markers[i].id){
                    $scope.markers[i].setPosition(new google.maps.LatLng( $scope.profilsSelected[i].position.latitude, $scope.profilsSelected[i].position.longitude));
                }
            }
            console.log("Markers after");
            console.log($scope.markers);


        });




	$scope.up = function()
    {
        Socket.sendMessage("UP","up1");
    }

	setInterval($scope.up,2000);
	$scope.markers = [];

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
        if(! $scope.profilsSelected.includes(ProfilSelected.get())) {
           $scope.profilsSelected[$scope.profilsSelected.length] = ProfilSelected.get();
        Tels.addTelToProfile(Tels.getIdCurrent(),ProfilSelected.get().id,$scope.profilsSelected);
       
        }
         console.log(marker);
			  marker.setMap($scope.map);
			  $scope.markers[$scope.markers.length] = marker;
			  $scope.cardVisible[ProfilSelected.get().id] = false;
        google.maps.event.addListener(marker, 'click', function () {
          $scope.$apply(function () {
              console.log("marker");
            console.log(marker.id);
            $scope.nom = ProfilSelected.get().nom;
            $scope.avatar = "img/test.png";
            $scope.cardVisible[marker.id] = true;
            for(var n = 0; n < $scope.profilsSelected.length; n++){
              if($scope.profilsSelected[n].id == marker.id){
                continue;
              }
              $scope.cardVisible[$scope.profilsSelected[n].id] = false;
            }
            $scope.duree = 90;
            $scope.batterie = 50;
            $scope.reseau = "On";
            console.log($scope.profilsSelected);
            console.log($scope.cardVisible);
          });
        });
		// console.log($scope.profilsSelected);
		$scope.showMap();
    });

})

.controller('ctrlListeProfils',function($scope,$state,$rootScope,$stateParams,Socket,Profils,ProfilSelected)
{

	console.log($stateParams.id);
	$scope.profils = Profils.all();
  $scope.positions = [];
  $scope.positions[0] = new google.maps.LatLng(43.612, 7.08);
  $scope.positions[1] = new google.maps.LatLng(43.610, 7.09);
  $scope.positions[2] = new google.maps.LatLng(43.608, 7.09);
  $scope.positions[3] = new google.maps.LatLng(43.609, 7.09);

  $scope.profilSelected = function(id)
	{
		var id = parseInt(id);
		var leProfil = Profils.get(id);
		ProfilSelected.set(leProfil);
		Socket.sendMessage("FOLLOW",$stateParams.idTel+"*"+leProfil.prenom+"*"+leProfil.nom);
            var marker = new google.maps.Marker({
                position: $scope.positions[ProfilSelected.get().id],
                animation: google.maps.Animation.DROP,
                icon: 'img/test.png',
                id: ProfilSelected.get().id
            });

        $rootScope.$broadcast('$createPositions', marker);
        $state.go("Map");

    }
});
