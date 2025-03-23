-- insert initial test data
-- the IDs are hardcoded to enable references between further test data
-- negative IDs are used to not interfere with user-entered data and allow clean deletion of test data

DELETE
FROM owner
WHERE id < 0;

INSERT INTO owner (id, first_name, last_name, description)
VALUES (-1, 'Wendy', 'Owner', 'Owner of multiple horses including Wendy.'),
       (-2, 'Not an', 'Owner', 'Owns no horses yet'),
       (-3, 'Third', 'Owner', 'Wait who is this?'),
       (-4, 'Fourth', 'Owner', 'Planning on buying a horse in the near future'),
       (-5, 'Fifth', 'Owner', 'I like horses lol'),
       (-6, 'Another', 'Owner', 'I own just one horse'),
       (-7, 'Leirbag', 'Rettan', 'Owns all of them (I wish)'),
       (-8, 'Not an', 'Owner', 'Not really an owner'),
       (-9, 'The', 'Great', 'Call me King'),
       (-10, 'Money', 'Maker', 'One day...');


DELETE
FROM horse
where id < 0;

INSERT INTO horse (id, name, description, date_of_birth, sex, owner_id, mother_id, father_id, image, mime_type)
VALUES (-1, 'Wendys Grandmother', 'The old one!', '1935-01-01', 'FEMALE', null, null, null, null, null),
       (-2, 'Wendys Grandfather', 'The chill one!', '1940-01-01', 'MALE', null, null, null, null, null),
       (-3, 'Wendys Mother', 'The famous one!', '1970-01-01', 'FEMALE', null, null, null, null, null),
       (-4, 'Wendys Father', 'The cool one!', '1970-01-01', 'MALE', null, -1, -2, null, null),
       (-5, 'Wendys Friend', 'The friendly one!', '1965-01-01', 'MALE', null, null, null, null, null),
       (-6, 'Wendy', 'The new one!', '2000-01-01', 'FEMALE', -1, -3, -4, null, null),
       (-7, 'Wendys Husband', 'The strong one!', '2000-01-01', 'MALE', null, null, null, null, null),
       (-8, 'Wendys first child', 'The first one!', '2020-01-01', 'MALE', -1, -6, null, null, null),
       (-9, 'Wendys second child', 'The second one!', '2022-01-01', 'MALE', -1, -6, null, null, null),
       (-10, 'Wendys third child', 'The third and last one!', '2024-01-01', 'FEMALE', -1, -6, null, X'ffd8ffe000104a46494600010100000100010000ffdb0084005050505055505a64645a7d8778877db9aa9b9baab9ffc8d7c8d7c8ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff015050505055505a64645a7d8778877db9aa9b9baab9ffc8d7c8d7c8ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffc000110800e1012a03012200021101031101ffc400700000030101010000000000000000000000000102030405100101000202030002020200070100000000010211032112314132511361227123424353627281910101010101000000000000000000000000000102031101010101010101010100000000000000000111120231412122ffda000c03010002110311003f004689b6931a8a47468aaa32bed5227eb482c0554011a38bd0d024e18402164a82191cda89b197d74ea15c30a654ea1611a23f13d8a8c99e9b69160234163481c4e7e8d390222b498d2023434bd1e819e86974b405a2d340035d32cbdba1865ec0b434a00de63db4d2828cb2953aadc681c970bb56afe9d3a1a435c9daf16fe3078c0426b5d27c41985585001694a901929329edd6473b4f63640455ef1a89deaaa5d6cb8b771acd6a2f48b2fe972ab69874c7c6abc5a6cb461d5478d6794d371d5ea9874e493b6b215c2cab65b840763408b4e1dc4680c686c6c0d8df6d6b3fa0a9078aa0d82bca9cb5270556e97953402fca8f340069e68cf9758f49db3cfd407463c9bc65579472715eec74c802d619db35a6d7a73f25ec17336d8e4e4c7b6f2504451658d9de93377e57695cac69b3138f3ad35861fdd4b618cfc6d96a30b7c69f2e795ebe27d638b1bb6379fcad8e339552b6e6a3010323110256934bcbb4adf95e868e1d65a4ea0d42b917900f185e10fc8fca027c13e11a6e16e027c4782b70f70190011a0000040a82695ee0a5b19a8c7ac9d58724d561e1bbbdea456f8fef1d5159734bea5639771ae3961ff695e1867f8d073637dbb25975fe98ce2b378d3c3ab6505db7145e7b3e2b7b61c935574c55e7ceab0ca5ef2acb0c6d5e3c7da69857cb2dae77c59b4b2cc6c8c2eb09e117c94e5eac5e39308ac6f71d5cec74ca36895498c9ed73d325e34b057d90f39ab29e33bd9e7f8b9d74f2529daca1f68d9523d26a2c0711b3837cb44e8b661c9682884e481e8e0ca497614809456b627c4197b5f89cc46594f5010ad5d5aae392d5e79633d08c22bea62b5b456d8e7bd4cd973f594a25ef1ff71be7879e1aab12b9b8b796ea797bb1133b8cf12b9eeaa09d35c6b2d6f5ba24dfa05f2656d66ab8658fb84dcc4a40c951a4c9d38e3e537b726aeb6ea996a42d31a78623f8f1463cb855ff002e0cff00a3f8d6750b29b9538e72fd87954691a252405679468551a97185e8e55dc517146ba5ceccb15d13a4ecb6902f4d48018512a1580469304dea5ac23a5849ab45563d6d2a2040c72b55643c6633b822f09be48e8dcbff00e31c3f0cf23f5cb863ff00828e6e4c3736c64767ab630cb1d50acda4eec9fd94d7d6931c7e2b22eef5b4ff001b6c7a692038ae36176edc98e97461ab7db6efc2ae60d24072c692ebe35b8497ca2e7faabd263196ee598b4b96fe2f585f90593c7a4b749319ecf69a232da81e8680a0b0c10486920b0a9186bb5695a029e88ea760d2299c54544e5111ad67f529aa4ff1fee9dba63efba8ad2e171434c392cf7dc3cf0dcf2c4562476e97c38eef9506bea61823febc2dee679a30bddcefc10efe597fb2ca6e16e5cad50ae797a3996959cd76c55974e377add7538f8e617dd6b8df0cac9dc51a64990ee532f49b416ab113288cedc964d66d5de493fb61756ec6a16db924676d39d7a6f8672fb9db99b618e8f598b354459528e4e8d651b6637415403d08a94ed4c8ab01243e98a2ca5e35b8119c8ad2c2e8ceca998d6a8cef4839f2bba2cd68f19d97bbb45078e7e17fa2b7a4f9406b9f16f29afa8bfe794c30f50a727f8670f0e4c30e3c80f9729a98c657ac3fdd4c9722b9ef2054e97b45f955115564ca595cb659755d311c98fd54aca56dc7755cff005d585b8ea035fbbd15c573b19d98e3686324dff6cee626786567b75ea39f27b398e59349863fa6f8a76bcb9f0d7c6dab3dc4e7ff000eee4563cb8e6c5badc88b25f826bf47678dd0d4cbfaa2df27bc7f45bc7f4cecb2ea908e99e155e31cd2b499d818db47a4cca53f21197265309b61fcf7f45cf96f291883bbf90ff918c3aa8d272b5db927b6f32453b9e9cf96777934caed96737017e5351cd72cb76ef4ac6abc71d81f1e5729aaabe33de36aba88b37dd974032b8dc35862c71de2d3cefcc6b2cb76f60ea99c9c777630de17e464056bbc7f515d328a9220d37fd51b2d4fd1ea026e18aa593f7478ff0074ae1fdd03cf9efac4639df36196160c72b8ddc07567c52cdc72dc5ae1cd9cabce4ca7940fa5c795f57dc6b7971c5c953047a1f9e35cf8cd569c39ee1e73fca52b7e2fe2ff003c7fb8cd332f1bb6997ee7aa37f2e1fe735f51a39579cdcf2831ea632384632676ed260cf2e397b73bb186b1546b05515104514028a4604616595a4f5b3b8ed370b7fe6145ce6b63bcfac6dd7dabc678c5027527a61cb3b8dd1cb3fc41cc0c91551510a15a4aa672ac17092a02d30cb1d3a119411cfdcaece0b8eba735c4b1b9637701d3c9c6e5b355db8f263962c6c99417ec4f1e723aacde15c1eaeaba7873df4ac934e3f7abeab3ee5b172a3ae2acb8d5e17e7cad6c994736b56c125ea1e535741a59e78efec65f060b7da98efb6d8d02478569944035d0d2895129b2ac02354f4b008d1e94013a3d1841160b8ef1ca29501e783ca6b2b08506460a8b9592e5ec558d882cd503046094d8b1419653a1c57e2ec633aca04fad73c77db39af95b67bf1db18a96634f2f3ffd9be1265197f16e6e0996585d0b2df8e8c64cba5dc319de99cddd5aacbcb5ed17f7e9637c3219e3abe519b5e3cb73c68bea7eb97d655ae29e5c7c738a9e86168d19035002a00400c000000000000000e4e5fcf266acaeeda48a464629292701acaabdc671a404a88c00000ac619cd57431caf701bf594cb1638f166d3f1b2fd2bed52dd74472672cceb699e527e7459e7f6d120e3b6c923a7564eeeeb9f738a33de572996546add74785b55ac38fbacbf9736594bbda1b6af933f3d418fa66d208a20360d0185420640003020600000091cb7583461cdee20c01814819015f665440691719c690532300000802b8caa00c31937aa79613cf459cd5dae67e5a6a7d2fc2d6bed6de77e74cf2535ed9f25f4506c284fd3acae40d3eb4618d6b281d4ec51d037002a00364060000000001e8039797f37539b9a7f922b30400c500522524178b48ce2c0cc8d0000014a190a9e54f1fab4f93d17ac63519abcacb370d20f548a1b217d209cab317b5401172a0c1a43678d3dd074900a80c00300000001900018737c001800114e180289f52002a280051800000802a022a393f12fbffc8037e59a7f61807afa436d87e3980838ce0006a000349e8015ffd9', 'image/jpeg');


