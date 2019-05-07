var goobiWorkflowJS = ( function( goobiWorkflow ) {
    'use strict';
    
    var _debug = false;
    var _defaults = {};
    
    goobiWorkflow.buttons = {
        /**
         * @description Method to initialize the buttons.
         * @method init
         */
    	init: function() {
            if ( _debug ) {
                console.log( 'Initializing: goobiWorkflow.buttons.init' );
            }

            if ( $( '.btn' ).hasClass( 'btn--toggle' ) ) {
                _setButtonToggleEvent();
            }

            // add buttons to pagination select pages
            $( '#myCheckboxes label' ).each( function () {
                $( this ).append( 
                    '<button type="button" class="btn btn--icon" data-toggle="star-color"><i class="fa fa-star" aria-hidden="true"></i></i></button>' +
                    '<button type="button" class="btn btn--icon" data-select="image"><i class="fa fa-picture-o" aria-hidden="true"></i></button>'
                );
            });

            // set select page button events
            _setSelectImageButtonEvent();
            _setRepresentativeButtonEvent();

            // set button events on ajax success
            if ( typeof jsf !== 'undefined' ) {
	            jsf.ajax.addOnEvent( function( data ) {
	                var ajaxstatus = data.status;
	                
	                switch ( ajaxstatus ) {                        
		                case "success":
                            if ( $( '.btn' ).hasClass( 'btn--toggle' ) ) {
                                _setButtonToggleEvent();
                            }
                            _setSelectImageButtonEvent();
                            _setRepresentativeButtonEvent();
		                	break;
	                }
	            } );
            }
        }
    };

    /**
     * @description Method to set the event listener to button toggle.
     * @method _setButtonToggleEvent
     */
    function _setButtonToggleEvent() {
        if ( _debug ) {
            console.log( 'EXECUTE: _setButtonToggleEvent' );
        }

        $( '.btn--toggle' ).off( 'click' ).on( 'click', function () {
            $( this ).next( 'div' ).slideToggle( 300 );
        });
    }
    
    /**
     * @description Method to set the event listener for representative star.
     * @method _setRepresentativeButtonEvent
     */
    function _setRepresentativeButtonEvent() {
        if ( _debug ) {
            console.log( 'EXECUTE: _setRepresentativeButtonEvent' );
        }

        $( '[data-toggle="star-color"]' ).off( 'click' ).on( 'click', function () {
            $( '[data-toggle="star-color"]' ).each( function() {
                $( this ).removeClass( 'active' );
            } );

            $( this ).toggleClass( 'active' );

            // TODO: 
            // - active state speichern
            // - commandButton klicken, um Bild zu setzen
        });
    }
    
    /**
     * @description Method to set the event listener to select image.
     * @method _setSelectImageButtonEvent
     */
    function _setSelectImageButtonEvent() {
        if ( _debug ) {
            console.log( 'EXECUTE: _setSelectImageButtonEvent' );
        }

        $( '[data-select="image"]' ).off( 'click' ).on( 'click', function () {
            var number = parseInt( $( this ).parent().attr( 'for' ).replace( 'myCheckboxes:', '' ) );
            
            $( '#jumpToImageAutocomplete_input' ).val( number + 1 );
            $( '#goButton' ).click();
        });
    }
    
    return goobiWorkflow;
    
} )( goobiWorkflowJS || {}, jQuery );