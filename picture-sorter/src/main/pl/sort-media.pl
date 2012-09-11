#!/usr/bin/perl -w

use strict;
use warnings;

use Time::Piece;
use File::Copy;
use File::Path;
use Log::Log4perl;

use constant ENABLED => 'yes';
use constant REMOVE_ORIGINAL => undef;
use constant WD => '/home/imgsorter';
use constant DEST => '/c/photos/incoming';
use constant LOG_CONFIG => 'etc/log4perl.conf';

chdir WD;

Log::Log4perl->init(LOG_CONFIG);

# expect list of FQ filenames of source images.
IMAGE: while(<>) {
    chomp;
    ## image filename
    my $image = $_;

    my $mediaFile = new MediaFile($_);
    my $valid = $mediaFile->validate();

    if($valid) {
	get_logger()->info("image accepted for processing: [$image]");
    } else {
	get_logger()->warn("image type not accepted for processing: [$image]");
    }

    ## extract create date of image, formatted as yyyy-mm-dd
    my $created = $mediaFile->create_date();

    if(not $created) {
	get_logger()->warn("unable to extract create date for image [$image]");
	next IMAGE;
    }

    my $dest_image = $mediaFile->format_dest_filepath(DEST);

    get_logger()->logdie("dest image already exists! ($dest_image) from ($image)")
	unless not -e $dest_image;

    if(ENABLED) {
	my $successful;
	get_logger()->info("copying [$image] to [$dest_image]");
	$successful = $mediaFile->copyToDest($dest_image);

	if(not $successful) {
	    get_logger()->logdie("unable to copy [$image] to [$dest_image]: $!");
	}

	$successful = undef;
	if(REMOVE_ORIGINAL) {
	    get_logger()->info("removing source image [$image] after successful copy.");
	    $successful = unlink $image;
	}

	if(not $successful) {
	    get_logger()->logdie("unable to remove source image [$image]: $!");
	}
    }
}
