#!/usr/bin/perl

use warnings FATAL => 'all';
use strict;

my $KEY = "ribbon-maker-$$";

print qx(aws s3 cp ribbon-maker.zip s3://helodali-exports/$KEY);

print qx(aws lambda update-function-code --function-name ribbon-maker --s3-bucket "helodali-exports" --s3-key $KEY);

print qx(aws s3 rm s3://helodali-exports/$KEY);
