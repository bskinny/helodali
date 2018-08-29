#!/usr/bin/perl
#
# Execute this with no args from the project root directory.

use warnings FATAL => 'all';
use strict;
my $prefix = "public-pages-generator";
my $version = "1.0.0";

my $KEY = "${prefix}-${version}-standalone-$$.jar";

qx(lein clean);
print qx(lein uberjar);
print qx(aws s3 cp "target/${prefix}-${version}-standalone.jar" s3://helodali-exports/$KEY);
print qx(aws lambda update-function-code --function-name $prefix --s3-bucket "helodali-exports" --s3-key $KEY);
print qx(aws s3 rm s3://helodali-exports/$KEY);
