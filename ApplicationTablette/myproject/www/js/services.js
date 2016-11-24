angular.module('starter.services', [])

.factory('Profils', function() {
  // Might use a resource here that returns a JSON array

  // Some fake testing data
  var profils = [];

  return {
    all: function() {
      return profils;
    },
    remove: function(chat) {
      profils.splice(profils.indexOf(profils), 1);
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
});
