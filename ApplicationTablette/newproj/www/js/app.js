angular.module('starter', ['ionic','starter.controllers','starter.services','ngCordova'])

.run(function($ionicPlatform) {
  $ionicPlatform.ready(function() {
    // Hide the accessory bar by default (remove this to show the accessory bar above the keyboard
    // for form inputs)
    if (window.cordova && window.cordova.plugins && window.cordova.plugins.Keyboard) {
      cordova.plugins.Keyboard.hideKeyboardAccessoryBar(true);
      cordova.plugins.Keyboard.disableScroll(true);

    }

    if (window.StatusBar) {
      // org.apache.cordova.statusbar required
      StatusBar.styleDefault();
    }
  });
})

.factory('GeoService', function($ionicPlatform, $cordovaGeolocation) {

  var positionOptions = {timeout: 10000, enableHighAccuracy: true};

  return {
    getPosition: function() {
      return $ionicPlatform.ready()
        .then(function() {
          return $cordovaGeolocation.getCurrentPosition(positionOptions);
        })
    }
  };

})

.config(function($stateProvider, $urlRouterProvider) {
 
  $stateProvider
	.state('Map', {
		url: '/map',
		templateUrl: 'templates/mapResto.html',
		controller : 'ctrlMap'
	})
	.state('SelectProfil', {
		url: '/selectProfil',
		templateUrl: 'templates/selectProfil.html',
        params: {
            idTel: null
        },
		controller : 'ctrlListeProfils'
	});
	
  $urlRouterProvider.otherwise("/map");
 
});


