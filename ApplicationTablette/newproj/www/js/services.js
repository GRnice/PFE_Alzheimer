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
  .factory('Tels', function() {
    // Might use a resource here that returns a JSON array

    // Some fake testing data
    var tels = [];
  var idTelCurrent;
    return {
      all: function() {
        return tels;
      },
      remove: function(idtel) {
        var j;
        for (var i = 0; i < tels.length; i++) {
        if(tels[i]==idtel){j=i;}
        }
        return tels.splice(j, 1);
      },
      get: function(idTel) {
        for (var i = 0; i < tels.length; i++) {
          if (tels[i]=== parseInt(idTel)) {
            return i;
          }
        }
        return null;
      },
      add: function(unNouveauTel)
      {
        tels[tels.length] = unNouveauTel;

      },

      addTelToProfile: function (IDTel,chatId,profilesSelected) {
        console.log("Chat Id");
        console.log(IDTel);
        console.log(chatId);
        for (var i = 0; i < profilesSelected.length; i++) {
          if (profilesSelected[i].id == chatId) {
            profilesSelected[i].idTel= IDTel;
            console.log(profilesSelected[i]);
          }
        }},
        set : function (idTel) {
          idTelCurrent = idTel;
        },
        getIdCurrent:function()
        {return idTelCurrent;
        }


    };
  })
.factory('Socket', function($state,$rootScope,Profils,Tels)
{
	var socket = io("http://127.0.0.1:2000");
	var id;
  var idTel;
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
			Profils.add({id : i, prenom: profile[0], nom: profile[1],idTel:null})
		}
		var tous = Profils.all();
		console.log(tous);
		$state.go("Map");
	});
    socket.on('NEWSESSION', function (data)
	{
		alert("NOUVELLE SESSION !");
		console.log("NOUVELLE SESSION");
		console.log(data);
		idTel = data;
    console.log($rootScope);
    $rootScope.$broadcast('$notifSession', true);
     //   $state.go('SelectProfil',{id : data});
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
		},
		data: function () {
			return idTel;
        }
	};
})

.factory('ProfilSelected', function($state,$rootScope,Profils,Tels)
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
