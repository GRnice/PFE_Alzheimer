angular.module('starter.services', [])

.factory('Profils', function() {
  // Might use a resource here that returns a JSON array

  // Some fake testing data
  var profils = [];

  return {
    all: function() {
      return profils;
    },
    remove: function(prof) {
      profils.splice(profils.indexOf(prof), 1);
    },
    get: function(chatId) {
      for (var i = 0; i < profils.length; i++) {
        if (profils[i].id === parseInt(chatId)) {
          return profils[i];
        }
      }
      return null;
    },
	add: function(unProfil)
	{
		profils[profils.length] = unProfil;
		return null;
	}
  };
})

.factory('Socket', function($state,Profils)
{
	var socket = io("http://192.168.1.13:2000");
	
	
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
		}
		var tous = Profils.all();
		console.log(tous);
	});
    socket.on('NEWSESSION', function (data)
	{
		alert("NOUVELLE SESSION !");
		console.log("NOUVELLE SESSION")
		console.log(data);
		$state.go('SelectProfil',{id : data});
	});
    
    socket.on('message', function(data) {
        console.log(data);
    });
	
	return{
		get: function(){
			return socket;
		},
		sendMessage: function(entete,corps)
		{
			socket.emit(entete,corps);
		}
	};
})

.factory('ProfilSelected', function()
{
	var profilSelected = 0;
	
		
return{
	
		get: function()
		{
			return profilSelected;
		},
		
		set: function(profil)
		{
			console.log(profil);
			profilSelected = profil;
			console.log(profilSelected);
		}
	};
});
