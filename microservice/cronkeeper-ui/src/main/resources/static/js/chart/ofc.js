OFC = {};
OFC.jquery = {
	name : "jQuery",
	version : function(src) {
		return $('#' + src)[0].get_version()
	},
	rasterize : function(src, dst) {
		$('#' + dst).replaceWith(OFC.jquery.image(src))
	},
	image : function(src) {
		return "<img class='imgBinary' src='data:image/png;base64,"
				+ $('#' + src)[0].get_img_binary() + "' />"
	},
	popup : function(src) {
		var img_win = window.open('', 'Image')
		with (img_win.document) {
			write('<html><head><title>Tipsdoo Chart Report Image</title></head><body>'
					+ OFC.jquery.image(src) + '</body></html>')
		}
		img_win.document.close();
	}
}

if (typeof (Control == "undefined")) {
	var Control = {
		OFC : OFC.jquery
	}
}

function save_image() {
	OFC.jquery.popup('divChange')
}
function moo() {
	
};