angular.module('starter.controllers', [])

.controller('DashCtrl', function($scope,$state,Profils)
{	
	var socket = io('http://127.0.0.1:2000');
	
	socket.on('connect', function(data)
	{
		console.log("connecte");
	});
	socket.on("PROFILES", function(data)
	{
		data = data.split("$");
		
		console.log(data);
		for (var i = 0 ; i < data.length ; i++)
		{
			profile = data[i];
			profile = profile.split(",");
			console.log(profile);
			Profils.add({id : i, prenom: profile[0], nom: profile[1]})
			$state.go('tab.chats');
			
		}
		var tous = Profils.all();
		console.log(tous);
	});
    socket.on('NEWSESSION', function (data)
	{
		console.log("NOUVELLE SESSION")
		
		console.log(data);
	});
    
    socket.on('message', function(data) {
        console.log(data);
    });
})

.controller('ChatsCtrl', function($scope, Profils)
{
	$scope.profils = Profils.all();
})

.controller('ChatDetailCtrl', function($scope, $stateParams, Chats) {
  $scope.chat = Chats.get($stateParams.chatId);
})

.controller('AccountCtrl', function($scope) {
  $scope.settings = {
    enableFriends: true
  };
});
