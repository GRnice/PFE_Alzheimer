angular.module('starter.controllers', [])

.controller('DashCtrl', function($scope,$state,Socket,Profils)
{	

})

.controller('ChatsCtrl', function($scope,$state,$stateParams,Socket,Profils,ProfilSelected)
{
	console.log($stateParams.id);
	$scope.profils = Profils.all();
	
	$scope.profilSelected = function(id)
	{
		var id = parseInt(id);
		var leProfil = Profils.get(id);
		ProfilSelected.set(leProfil);
		Socket.sendMessage("FOLLOW",$stateParams.id);
	}
})

.controller('ChatDetailCtrl', function($scope, $stateParams, Chats) {
  $scope.chat = Chats.get($stateParams.chatId);
})

.controller('AccountCtrl', function($scope) {
  $scope.settings = {
    enableFriends: true
  };
});
